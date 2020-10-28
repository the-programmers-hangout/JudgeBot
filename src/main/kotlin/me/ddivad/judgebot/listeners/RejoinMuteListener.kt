package me.ddivad.judgebot.listeners

import com.gitlab.kordlib.core.event.guild.MemberJoinEvent
import kotlinx.coroutines.runBlocking
import me.ddivad.judgebot.dataclasses.InfractionType
import me.ddivad.judgebot.services.LoggingService
import me.ddivad.judgebot.services.MuteService
import me.ddivad.judgebot.services.RoleState
import me.jakejmattson.discordkt.api.dsl.listeners

fun onMemberRejoinWithMute(muteService: MuteService, loggingService: LoggingService) = listeners {
    on<MemberJoinEvent> {
        val member = this.member
        val guild = this.getGuild()
        // RunBlocking needed here for listener to work correctly (maybe an issue with listeners & coroutines exists)
        runBlocking {
            if (muteService.checkRoleState(guild, member, InfractionType.Mute) == RoleState.Tracked) {
                muteService.handleRejoinMute(guild, member)
                loggingService.rejoinMute(guild, member.asUser(), RoleState.Tracked)
            }
        }
    }
}