package me.ddivad.judgebot.services.infractions

import dev.kord.core.behavior.ban
import dev.kord.core.entity.Guild
import dev.kord.core.entity.User
import me.ddivad.judgebot.dataclasses.Ban
import me.ddivad.judgebot.services.DatabaseService
import me.ddivad.judgebot.services.LoggingService
import me.jakejmattson.discordkt.annotations.Service

@Service
class BanService(
    private val databaseService: DatabaseService,
    private val loggingService: LoggingService,
) {
    suspend fun banUser(target: User, guild: Guild, ban: Ban, deleteDays: Int = 0) {
        val guildMember = databaseService.users.getOrCreateUser(target, guild)
        val banRecord = Ban(target.id.toString(), ban.moderator, ban.reason)
        guild.ban(target.id) {
            deleteMessagesDays = deleteDays
            reason = ban.reason
        }
        databaseService.guilds.addBan(guild, banRecord)
        databaseService.users.addBanRecord(guild, guildMember, banRecord)
    }

    suspend fun unbanUser(target: User, guild: Guild, thinIce: Boolean = false) {
        val guildMember = databaseService.users.getOrCreateUser(target, guild)
        guild.unban(target.id).let {
            databaseService.guilds.removeBan(guild, target.id.toString())
            databaseService.users.addUnbanRecord(guild, guildMember, thinIce)
            loggingService.userUnbanned(guild, target)
        }
    }
}