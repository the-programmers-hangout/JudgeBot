package me.ddivad.judgebot.services.infractions

import dev.kord.common.exception.RequestException
import dev.kord.core.behavior.edit
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Member
import me.ddivad.judgebot.dataclasses.*
import me.ddivad.judgebot.embeds.createInfractionEmbed
import me.ddivad.judgebot.services.DatabaseService
import me.ddivad.judgebot.services.LoggingService
import me.jakejmattson.discordkt.annotations.Service
import me.jakejmattson.discordkt.extensions.sendPrivateMessage

@Service
class InfractionService(
    private val configuration: Configuration,
    private val databaseService: DatabaseService,
    private val loggingService: LoggingService,
    private val banService: BanService,
    private val muteService: MuteService
) {

    suspend fun infract(target: Member, guild: Guild, userRecord: GuildMember, infraction: Infraction): Infraction {
        var rule: Rule? = null
        if (infraction.ruleNumber != null) {
            rule = databaseService.guilds.getRule(guild, infraction.ruleNumber)
        }
        return databaseService.users.addInfraction(guild, userRecord, infraction).also {
            try {
                target.asUser().sendPrivateMessage {
                    createInfractionEmbed(guild, configuration[guild.id]!!, target, userRecord, it, rule)
                }
            } catch (ex: RequestException) {
                loggingService.dmDisabled(guild, target.asUser())
            }
            loggingService.infractionApplied(guild, target.asUser(), it)
            applyPunishment(guild, target, it)
        }
    }

    private suspend fun applyPunishment(guild: Guild, target: Member, infraction: Infraction) {
        when (infraction.punishment?.punishment) {
            PunishmentType.NONE -> return
            PunishmentType.MUTE -> muteService.applyInfractionMute(target, infraction.punishment?.duration!!)
            PunishmentType.BAN -> {
                val punishment = Ban(target.id.toString(), infraction.moderator, infraction.reason)
                banService.banUser(target, guild, punishment)
            }
        }
    }

    suspend fun badName(member: Member) {
        val badNames = mutableListOf(
            "Stephen", "Bob", "Joe", "Timmy", "Arnold", "Jeff", "Tim", "Doug"
        )
        member.edit { nickname = badNames.random() }
    }
}
