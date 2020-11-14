package me.ddivad.judgebot.listeners

import com.gitlab.kordlib.core.event.guild.MemberJoinEvent
import me.ddivad.judgebot.dataclasses.InfractionType
import me.ddivad.judgebot.services.LoggingService
import me.ddivad.judgebot.services.infractions.MuteService
import me.ddivad.judgebot.services.infractions.RoleState
import me.jakejmattson.discordkt.api.dsl.listeners

@Suppress("unused")
fun onMemberRejoinWithMute(muteService: MuteService, loggingService: LoggingService) = listeners {
    on<MemberJoinEvent> {
        val member = this.member
        val guild = this.getGuild()
        if (muteService.checkRoleState(guild, member) == RoleState.Tracked) {
            muteService.handleRejoinMute(guild, member)
            loggingService.rejoinMute(guild, member.asUser(), RoleState.Tracked)
        }
    }
}