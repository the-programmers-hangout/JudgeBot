package me.ddivad.judgebot.services.infractions

import com.gitlab.kordlib.core.behavior.ban
import com.gitlab.kordlib.core.entity.Guild
import com.gitlab.kordlib.core.entity.Member
import com.gitlab.kordlib.core.entity.User
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
class BanService(private val databaseService: DatabaseService,
                 private val loggingService: LoggingService,
                 private val configuration: Configuration,
                 private val discord: Discord) {
    private val banTracker = hashMapOf<Pair<UserId, GuildID>, Job>()
    private fun toKey(user: User, guild: Guild): Pair<GuildID, UserId> = user.id.value to guild.id.value

    suspend fun banUser(target: User, guild: Guild, punishment: Punishment, deleteDays: Int = 0) {
        guild.ban(target.id) {
            deleteMessagesDays = deleteDays
            reason = punishment.reason
        }
        databaseService.guilds.addBan(guild, Ban(target.id.value, punishment.moderator, punishment.reason))
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
            databaseService.guilds.removePunishment(guild, user.id.value, InfractionType.Ban)
            banTracker[key]?.cancel()
        }
        guild.unban(user.id)
        databaseService.guilds.removeBan(guild, user.id.value)
        loggingService.userUnbanned(guild, user)
    }

    suspend fun initialiseBanTimers() {
        configuration.guildConfigurations.forEach { config ->
            val guild = config.value.id.toSnowflake().let { discord.api.getGuild(it) } ?: return@forEach
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

    }
}