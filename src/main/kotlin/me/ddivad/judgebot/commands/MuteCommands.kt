package me.ddivad.judgebot.commands

import dev.kord.common.exception.RequestException
import dev.kord.x.emoji.Emojis
import dev.kord.x.emoji.addReaction
import me.ddivad.judgebot.arguments.LowerMemberArg
import me.ddivad.judgebot.dataclasses.Permissions
import me.ddivad.judgebot.extensions.testDmStatus
import me.ddivad.judgebot.services.infractions.MuteService
import me.ddivad.judgebot.services.infractions.RoleState
import me.ddivad.judgebot.util.timeToString
import me.jakejmattson.discordkt.arguments.EveryArg
import me.jakejmattson.discordkt.arguments.TimeArg
import me.jakejmattson.discordkt.arguments.UserArg
import me.jakejmattson.discordkt.commands.commands
import kotlin.math.roundToLong

@Suppress("unused")
fun createMuteCommands(muteService: MuteService) = commands("Mute") {
    command("mute") {
        description = "Mute a user for a specified time."
        requiredPermission = Permissions.MODERATOR
        execute(LowerMemberArg, TimeArg("Time"), EveryArg("Reason")) {
            val (targetMember, length, reason) = args
            try {
                targetMember.testDmStatus()
                this.message.addReaction(Emojis.whiteCheckMark)
            } catch (ex: RequestException) {
                this.message.addReaction(Emojis.x)
                respond("${targetMember.mention} has DMs disabled and won't receive message.")
            }
            muteService.applyMuteAndSendReason(targetMember, length.roundToLong() * 1000, reason)
            respond("User ${targetMember.mention} has been muted")
        }
    }

    command("unmute") {
        description = "Unmute a user."
        requiredPermission = Permissions.MODERATOR
        execute(LowerMemberArg) {
            val targetMember = args.first
            if (muteService.checkRoleState(guild, targetMember) == RoleState.None) {
                respond("User ${targetMember.mention} isn't muted")
                return@execute
            }

            muteService.removeMute(guild, targetMember.asUser())
            respond("User ${args.first.mention} has been unmuted")
        }
    }

    command("gag") {
        description = "Mute a user for 5 minutes while you deal with something"
        requiredPermission = Permissions.MODERATOR
        execute(LowerMemberArg) {
            val targetMember = args.first
            if (muteService.checkRoleState(guild, targetMember) == RoleState.Tracked) {
                respond("User ${targetMember.mention} is already muted")
                return@execute
            }
            val time = 1000L * 60 * 5
            muteService.gag(guild, targetMember, author)

            respond("${targetMember.mention} has been muted for ${timeToString(time)}")
        }
    }
}