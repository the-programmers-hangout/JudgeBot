package me.ddivad.judgebot.services.infractions

import dev.kord.core.behavior.ban
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Member
import dev.kord.core.entity.User
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.ddivad.judgebot.dataclasses.*
import me.ddivad.judgebot.services.DatabaseService
import me.ddivad.judgebot.services.LoggingService
import me.jakejmattson.discordkt.api.Discord
import me.jakejmattson.discordkt.api.annotations.Service
import me.jakejmattson.discordkt.api.extensions.toSnowflake
import org.joda.time.DateTime

@Service
class BanService(
    private val databaseService: DatabaseService,
    private val loggingService: LoggingService,
    private val configuration: Configuration,
    private val discord: Discord
) {
    private val banTracker = hashMapOf<Pair<UserId, GuildID>, Job>()
    private fun toKey(user: User, guild: Guild): Pair<GuildID, UserId> = user.id.asString to guild.id.asString

    suspend fun banUser(target: User, guild: Guild, punishment: Punishment, deleteDays: Int = 0) {
        guild.ban(target.id) {
            deleteMessagesDays = deleteDays
            reason = punishment.reason
        }
        databaseService.guilds.addBan(guild, Ban(target.id.asString, punishment.moderator, punishment.reason))
        if (punishment.clearTime != null) {
            databaseService.guilds.addPunishment(guild.asGuild(), punishment)
            val key = toKey(target, guild)
            banTracker[key] = GlobalScope.launch {
                delay(punishment.clearTime)
                guild.unban(target.id)
            }
            loggingService.userBannedWithTimer(guild, target, punishment)
        }
    }

    suspend fun unbanUser(guild: Guild, user: User) {
        val key = toKey(user, guild)
        if (databaseService.guilds.getPunishmentsForUser(guild, user).any { it.type == InfractionType.Ban }) {
            databaseService.guilds.removePunishment(guild, user.id.asString, InfractionType.Ban)
            banTracker[key]?.cancel()
        }
        guild.unban(user.id)
        databaseService.guilds.removeBan(guild, user.id.asString)
        loggingService.userUnbanned(guild, user)
    }

    suspend fun initialiseBanTimers() {
        try {
            configuration.guildConfigurations.forEach { config ->
                val guild = config.value.id.toSnowflake().let { discord.kord.getGuild(it) } ?: return@forEach
                databaseService.guilds.getPunishmentsForGuild(guild, InfractionType.Ban).forEach() {
                    if (it.clearTime != null) {
                        val difference = it.clearTime - DateTime.now().millis
                        guild.kord.getUser(it.userId.toSnowflake())?.let { user ->
                            val key = toKey(user, guild)
                            banTracker[key] = GlobalScope.launch {
                                delay(difference)
                                guild.unban(user.id)
                            }
                        }
                        loggingService.initialiseBans(guild)
                    }
                }
            }
        } catch (ex: Exception) {
            println(ex.message)
        }
    }
}