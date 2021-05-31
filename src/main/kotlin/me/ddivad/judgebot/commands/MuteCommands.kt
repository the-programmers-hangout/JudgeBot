package me.ddivad.judgebot.commands

import com.gitlab.kordlib.common.exception.RequestException
import com.gitlab.kordlib.kordx.emoji.Emojis
import com.gitlab.kordlib.kordx.emoji.addReaction
import me.ddivad.judgebot.arguments.LowerMemberArg
import me.ddivad.judgebot.extensions.testDmStatus
import me.ddivad.judgebot.services.*
import me.ddivad.judgebot.services.infractions.MuteService
import me.ddivad.judgebot.services.infractions.RoleState
import me.ddivad.judgebot.util.timeToString
import me.jakejmattson.discordkt.api.arguments.EveryArg
import me.jakejmattson.discordkt.api.arguments.TimeArg
import me.jakejmattson.discordkt.api.dsl.commands
import kotlin.math.roundToLong

@Suppress("unused")
fun createMuteCommands(muteService: MuteService) = commands("Mute") {
    guildCommand("mute") {
        description = "Mute a user for a specified time."
        requiredPermissionLevel = PermissionLevel.Moderator
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

    guildCommand("unmute") {
        description = "Unmute a user."
        requiredPermissionLevel = PermissionLevel.Moderator
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

    guildCommand("gag") {
        description = "Mute a user for 5 minutes while you deal with something"
        requiredPermissionLevel = PermissionLevel.Moderator
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