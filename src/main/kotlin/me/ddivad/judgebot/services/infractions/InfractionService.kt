package me.ddivad.judgebot.services.infractions

import com.gitlab.kordlib.core.entity.Guild
import com.gitlab.kordlib.core.entity.Member
import me.ddivad.judgebot.dataclasses.*
import me.ddivad.judgebot.embeds.createInfractionEmbed
import me.ddivad.judgebot.services.DatabaseService
import me.ddivad.judgebot.services.LoggingService
import me.ddivad.judgebot.services.MuteService
import me.jakejmattson.discordkt.api.annotations.Service
import me.jakejmattson.discordkt.api.extensions.sendPrivateMessage

@Service
class InfractionService(private val configuration: Configuration,
                        private val databaseService: DatabaseService,
                        private val loggingService: LoggingService,
                        private val muteService: MuteService) {
    suspend fun infract(target: Member, guild: Guild, userRecord: GuildMember, infraction: Infraction): Infraction {
        var rule: Rule? = null
        if (infraction.ruleNumber != null) {
            rule = databaseService.guilds.getRule(guild, infraction.ruleNumber)
        }
        return databaseService.users.addInfraction(guild, userRecord, infraction).also {
            applyPunishment(guild, target, userRecord, it)
            target.asUser().sendPrivateMessage {
                createInfractionEmbed(guild, configuration[guild.id.longValue]!!, target, it, rule)
            }
            loggingService.infractionApplied(guild, target.asUser(), it)
        }
    }

    private suspend fun applyPunishment(guild: Guild, target: Member, guildMember: GuildMember, infraction: Infraction) {
        when(infraction.punishment?.punishment) {
            PunishmentType.MUTE -> muteService.applyMute(target, infraction.punishment!!.duration!!, infraction.reason, infraction.type)
            PunishmentType.BAN -> databaseService.guilds.banUser(guild, target.id.value, infraction.moderator, infraction.reason)
        }
    }
}