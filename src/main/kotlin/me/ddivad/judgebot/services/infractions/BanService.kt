package me.ddivad.judgebot.services.infractions

import com.gitlab.kordlib.core.behavior.ban
import com.gitlab.kordlib.core.entity.Guild
import com.gitlab.kordlib.core.entity.Member
import com.gitlab.kordlib.core.entity.User
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.ddivad.judgebot.dataclasses.Ban
import me.ddivad.judgebot.dataclasses.InfractionType
import me.ddivad.judgebot.dataclasses.Punishment
import me.ddivad.judgebot.services.DatabaseService
import me.ddivad.judgebot.services.LoggingService
import me.jakejmattson.discordkt.api.annotations.Service

@Service
class BanService(private val databaseService: DatabaseService,
                 private val loggingService: LoggingService) {
    private val banTracker = hashMapOf<Int, Job>()
    private suspend fun toKey(member: Member): Pair<GuildID, UserId> = member.guild.id.value to member.asUser().id.value

    suspend fun banUser(target: Member, guild: Guild, punishment: Punishment, deleteDays: Int = 1) {
        guild.ban(target.id) {
            deleteMessagesDays = deleteDays
            reason = punishment.reason
        }
        databaseService.guilds.addBan(guild, target.id.value, Ban(target.id.value, punishment.moderator, punishment.reason))
        if (punishment.clearTime != null) {
            databaseService.guilds.addPunishment(guild.asGuild(), punishment)
            banTracker[punishment.id] = GlobalScope.launch {
                delay(punishment.clearTime)
                guild.unban(target.id)
            }
            loggingService.userBannedWithTimer(guild, target, punishment)
        }
    }

    suspend fun unbanUser(guild: Guild, user: User) {
        if (databaseService.guilds.getPunishmentsForUser(guild, user).any { it.type == InfractionType.Ban }) {
            val punishment = databaseService.guilds.getPunishmentByType(guild, user.id.value, InfractionType.Ban).first()
            databaseService.guilds.removePunishment(guild, user.id.value, InfractionType.Ban)
            banTracker[punishment.id]?.cancel()
        }
        guild.unban(user.id)
        databaseService.guilds.removeBan(guild, user.id.value)
        loggingService.userUnbanned(guild, user)
    }
}