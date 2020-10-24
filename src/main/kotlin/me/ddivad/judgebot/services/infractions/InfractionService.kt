package me.ddivad.judgebot.services.infractions

import com.gitlab.kordlib.core.entity.Guild
import com.gitlab.kordlib.core.entity.Member
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.dataclasses.GuildMember
import me.ddivad.judgebot.dataclasses.Infraction
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

        target.asUser().sendPrivateMessage {
            createInfractionEmbed()
        }
        loggingService.infractionApplied(guild, target.asUser(), infraction)
        return databaseService.users.addInfraction(guild, userRecord, infraction)
    }
}