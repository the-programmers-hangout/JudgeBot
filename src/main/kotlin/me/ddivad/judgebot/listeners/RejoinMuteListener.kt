package me.ddivad.judgebot.listeners

import dev.kord.core.event.guild.MemberJoinEvent
import me.ddivad.judgebot.services.LoggingService
import me.ddivad.judgebot.services.infractions.MuteService
import me.ddivad.judgebot.services.infractions.MuteState
import me.jakejmattson.discordkt.dsl.listeners

@Suppress("unused")
fun onMemberRejoinWithMute(muteService: MuteService, loggingService: LoggingService) = listeners {
    on<MemberJoinEvent> {
        val member = this.member
        val guild = this.getGuild()
        if (muteService.checkMuteState(guild, member) == MuteState.Tracked) {
            muteService.handleRejoinMute(guild, member)
            loggingService.rejoinMute(guild, member.asUser(), MuteState.Tracked)
        }
    }
}