package me.ddivad.judgebot.commands

import me.ddivad.judgebot.arguments.LowerMemberArg
import me.ddivad.judgebot.dataclasses.InfractionType
import me.ddivad.judgebot.services.*
import me.jakejmattson.discordkt.api.arguments.EveryArg
import me.jakejmattson.discordkt.api.arguments.TimeArg
import me.jakejmattson.discordkt.api.dsl.commands
import kotlin.math.roundToLong

fun createMuteCommands(muteService: MuteService) = commands("Mute") {
    guildCommand("mute") {
        description = "Mute a user for a specified time."
        requiredPermissionLevel = PermissionLevel.Staff
        execute(LowerMemberArg, TimeArg, EveryArg) {
            muteService.applyMute(args.first, args.second.roundToLong() * 1000, args.third)
            respond("User ${args.first.username} has been muted")
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

            guild.getBan(targetMember.id).reason
            muteService.removeMute(targetMember)
            respond("User ${args.first.username} has been unmuted")
        }
    }

    guildCommand("gag") {
        description = "Mute a user for 5 minutes while you deal with something"
        requiredPermissionLevel = PermissionLevel.Staff
        execute(LowerMemberArg) {
            muteService.applyMute(
                    args.first,
                    1000 * 60 * 5,
                    "You've been muted temporarily so that a mod can handle something.")
        }
    }
}