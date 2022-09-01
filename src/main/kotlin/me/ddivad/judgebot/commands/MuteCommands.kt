package me.ddivad.judgebot.commands

import dev.kord.common.exception.RequestException
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.response.respond
import kotlinx.datetime.toKotlinInstant
import me.ddivad.judgebot.arguments.LowerMemberArg
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.dataclasses.Permissions
import me.ddivad.judgebot.extensions.getHighestRolePosition
import me.ddivad.judgebot.extensions.testDmStatus
import me.ddivad.judgebot.services.infractions.MuteService
import me.ddivad.judgebot.services.infractions.RoleState
import me.jakejmattson.discordkt.arguments.EveryArg
import me.jakejmattson.discordkt.arguments.MemberArg
import me.jakejmattson.discordkt.arguments.TimeArg
import me.jakejmattson.discordkt.commands.commands
import me.jakejmattson.discordkt.extensions.TimeStamp
import java.time.Instant
import kotlin.math.roundToLong

@Suppress("unused")
fun createMuteCommands(muteService: MuteService, configuration: Configuration) = commands("Mute") {
    slash("mute", "Mute a user for a specified time.", Permissions.MODERATOR) {
        execute(LowerMemberArg("Member", "Target Member"), TimeArg("Time"), EveryArg("Reason")) {
            val (targetMember, length, reason) = args
            val interactionResponse = interaction?.deferPublicResponse()
            val dmEnabled: Boolean = try {
                targetMember.testDmStatus()
                true
            } catch (ex: RequestException) {
                false
            }
            muteService.applyMuteAndSendReason(targetMember, length.roundToLong() * 1000, reason)
            interactionResponse?.respond {
                content =
                    "User ${targetMember.mention} has been muted ${if (!dmEnabled) "\n**Note**: user has DMs disabled and won't receive bot messages." else ""}"
            }
        }
    }

    slash("unmute", "Unmute a user.", Permissions.MODERATOR) {
        execute(MemberArg) {
            val targetMember = args.first
            val interactionResponse = interaction?.deferPublicResponse()
            if (muteService.checkRoleState(guild, targetMember) == RoleState.None) {
                respond("User ${targetMember.mention} isn't muted")
                return@execute
            }
            muteService.removeMute(guild, targetMember.asUser())
            interactionResponse?.respond {
                content = "User ${args.first.mention} has been unmuted"
            }
        }
    }

    slash("timeout", "Time a user out", Permissions.MODERATOR) {
        execute(LowerMemberArg("Member", "Target Member"), TimeArg) {
            val (member, time) = args
            val duration = Instant.ofEpochMilli(Instant.now().toEpochMilli() + (time - 2).toLong() * 1000)
            member.edit {
                communicationDisabledUntil = duration.toKotlinInstant()
            }
            respond("Member timed out")
        }
    }

    user("Gag", "gag", "Mute a user for 5 minutes", Permissions.MODERATOR) {
        val targetMember = arg.asMemberOrNull(guild.id) ?: return@user
        val muteDuration = configuration[guild.id]?.infractionConfiguration?.gagDuration ?: return@user
        if (targetMember.getHighestRolePosition() > author.asMember(guild.id).getHighestRolePosition()) {
            respond("Missing required permission for target user")
            return@user
        }
        if (muteService.checkRoleState(guild, targetMember) == RoleState.Tracked) {
            respond("User ${targetMember.mention} is already muted")
            return@user
        }
        muteService.gag(guild, targetMember, author)
        respond("${targetMember.mention} has been muted for ${TimeStamp.at(Instant.ofEpochMilli(muteDuration))}")
    }
}