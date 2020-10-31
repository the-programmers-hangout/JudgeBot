package me.ddivad.judgebot.services.infractions

import com.gitlab.kordlib.core.entity.Guild
import com.gitlab.kordlib.core.entity.Member
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.dataclasses.GuildMember
import me.ddivad.judgebot.dataclasses.Infraction
import me.ddivad.judgebot.dataclasses.Rule
import me.ddivad.judgebot.embeds.createInfractionEmbed
import me.ddivad.judgebot.services.DatabaseService
import me.ddivad.judgebot.services.LoggingService
import me.jakejmattson.discordkt.api.annotations.Service
import me.jakejmattson.discordkt.api.extensions.sendPrivateMessage

@Service
class InfractionService(private val configuration: Configuration,
                        private val databaseService: DatabaseService,
                        private val loggingService: LoggingService) {
    suspend fun infract(target: Member, guild: Guild, userRecord: GuildMember, infraction: Infraction): GuildMember {
        var rule: Rule? = null

        if (infraction.ruleNumber != null) {
            rule = databaseService.guilds.getRule(guild, infraction.ruleNumber)
        }

        target.asUser().sendPrivateMessage {
            createInfractionEmbed(guild, target, infraction, rule)
        }
        loggingService.infractionApplied(guild, target.asUser(), infraction)
        applyPunishment(target,guild, userRecord)
        return databaseService.users.addInfraction(guild, userRecord, infraction)
    }

    private suspend fun applyPunishment(target: Member, guild: Guild, guildMember: GuildMember) {
        val punishmentLevels = configuration[guild.id.longValue]?.punishments
        val punishmentForPoints = punishmentLevels?.filter {
            it.points <= guildMember.getGuildInfo(guild.id.value)?.points!!
        }?.maxByOrNull { it.points }
        print(punishmentForPoints)
    }
}