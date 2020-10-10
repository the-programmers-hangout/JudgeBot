package me.ddivad.judgebot.commands

import me.ddivad.judgebot.dataclasses.InfractionType
import me.ddivad.judgebot.services.*
import me.jakejmattson.discordkt.api.arguments.EveryArg
import me.jakejmattson.discordkt.api.arguments.MemberArg
import me.jakejmattson.discordkt.api.arguments.TimeArg
import me.jakejmattson.discordkt.api.dsl.commands
import kotlin.math.roundToLong

fun createMuteCommands(muteService: MuteService) = commands("Mute") {
    command("mute") {
        description = "Mute a user for a specified time."
        requiresGuild = true
        requiredPermissionLevel = PermissionLevel.Staff
        execute(MemberArg, TimeArg, EveryArg) {
            muteService.applyMute(args.first, args.second.roundToLong() * 1000, args.third, InfractionType.Mute)
            respond("User ${args.first.username} has been muted")
        }
    }

    command("unmute") {
        description = "Unmute a user."
        requiresGuild = true
        requiredPermissionLevel = PermissionLevel.Staff
        execute(MemberArg) {
            val targetMember = args.first
            if (muteService.checkRoleState(targetMember, InfractionType.Mute) == RoleState.None)
                return@execute respond("User ${targetMember.mention} isn't muted")

            muteService.removeMute(targetMember, InfractionType.Mute)
            respond("User ${args.first.username} has been unmuted")
        }
    }

    command("gag") {
        description = "Mute a user for 5 minutes while you deal with something"
        requiresGuild = true
        requiredPermissionLevel = PermissionLevel.Staff
        execute(MemberArg) {
            muteService.applyMute(
                    args.first,
                    1000 * 60 * 5,
                    "You've been muted temporarily so that a mod can handle something.",
                    InfractionType.Mute)
        }
    }
}