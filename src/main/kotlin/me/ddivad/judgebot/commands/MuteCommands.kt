package me.ddivad.judgebot.commands

import com.gitlab.kordlib.common.exception.RequestException
import me.ddivad.judgebot.arguments.LowerMemberArg
import me.ddivad.judgebot.dataclasses.InfractionType
import me.ddivad.judgebot.extensions.testDmStatus
import me.ddivad.judgebot.services.*
import me.ddivad.judgebot.services.infractions.MuteService
import me.ddivad.judgebot.services.infractions.RoleState
import me.ddivad.judgebot.util.timeToString
import me.jakejmattson.discordkt.api.arguments.EveryArg
import me.jakejmattson.discordkt.api.arguments.TimeArg
import me.jakejmattson.discordkt.api.dsl.commands
import kotlin.math.roundToLong

fun createMuteCommands(muteService: MuteService) = commands("Mute") {
    guildCommand("mute") {
        description = "Mute a user for a specified time."
        requiredPermissionLevel = PermissionLevel.Staff
        execute(LowerMemberArg, TimeArg, EveryArg) {
            val (targetMember, length, reason) = args
            try {
                targetMember.testDmStatus()
            } catch (ex: RequestException) {
                respond("Unable to contact the target user. Infraction cancelled.")
                return@execute
            }
            muteService.applyMute(targetMember, length.roundToLong() * 1000, reason)
            respond("User ${targetMember.mention} has been muted")
        }
    }

    guildCommand("unmute") {
        description = "Unmute a user."
        requiredPermissionLevel = PermissionLevel.Staff
        execute(LowerMemberArg) {
            val targetMember = args.first
            if (muteService.checkRoleState(guild, targetMember, InfractionType.Mute) == RoleState.None) {
                respond("User ${targetMember.mention} isn't muted")
                return@execute
            }

            muteService.removeMute(targetMember)
            respond("User ${args.first.username} has been unmuted")
        }
    }

    guildCommand("gag") {
        description = "Mute a user for 5 minutes while you deal with something"
        requiredPermissionLevel = PermissionLevel.Staff
        execute(LowerMemberArg) {
            val targetMember = args.first
            try {
                targetMember.testDmStatus()
            } catch (ex: RequestException) {
                respond("Unable to contact the target user. Infraction cancelled.")
                return@execute
            }
            if (muteService.checkRoleState(guild, targetMember, InfractionType.Mute) == RoleState.Tracked) {
                respond("User ${targetMember.mention} is already muted")
                return@execute
            }
            val time = 1000L * 60 * 5
            muteService.applyMute(
                    args.first,
                    time,
                    "You've been muted temporarily so that a mod can handle something.")

            respond("${targetMember.mention} has been muted for ${timeToString(time)}")
        }
    }
}