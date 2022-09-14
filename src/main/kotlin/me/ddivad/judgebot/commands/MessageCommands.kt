package me.ddivad.judgebot.commands

import dev.kord.common.exception.RequestException
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.rest.builder.message.modify.embed
import dev.kord.x.emoji.Emojis
import me.ddivad.judgebot.arguments.LowerMemberArg
import me.ddivad.judgebot.dataclasses.Info
import me.ddivad.judgebot.dataclasses.Permissions
import me.ddivad.judgebot.embeds.createInformationEmbed
import me.ddivad.judgebot.extensions.testDmStatus
import me.ddivad.judgebot.services.DatabaseService
import me.jakejmattson.discordkt.arguments.EveryArg
import me.jakejmattson.discordkt.arguments.IntegerArg
import me.jakejmattson.discordkt.arguments.MemberArg
import me.jakejmattson.discordkt.commands.subcommand
import me.jakejmattson.discordkt.extensions.sendPrivateMessage

@Suppress("unused")
fun createInformationCommands(databaseService: DatabaseService) = subcommand("Message", Permissions.STAFF) {
    sub("send", "Send an information message to a guild member") {
        execute(LowerMemberArg("Member", "Target Member"), EveryArg("Content")) {
            val (target, content) = args
            val interactionResponse = interaction?.deferPublicResponse() ?: return@execute
            var dmEnabled: Boolean
            try {
                target.testDmStatus()
                dmEnabled = true
                val user = databaseService.users.getOrCreateUser(target, guild)
                val information = Info(content, author.id.toString())
                databaseService.users.addInfo(guild, user, information)
                target.sendPrivateMessage {
                    createInformationEmbed(guild, target, information)
                }
            } catch (ex: RequestException) {
                dmEnabled = false
            }

            interactionResponse.respond {
                embed {
                    color = discord.configuration.theme
                    title = "Message Command: ${if (dmEnabled) Emojis.whiteCheckMark else Emojis.x}"
                    description = if (dmEnabled) {
                        "Message added and sent to ${target.mention}"
                    } else {
                        "User ${target.mention} has DMs disabled. Message not added or sent."
                    }
                }
            }
        }
    }

    sub("remove", "Remove a message record from a member. Only removes from history record, user DM will remain.") {
        execute(MemberArg, IntegerArg("ID", "ID of message record to delete")) {
            val (target, id) = args
            val interactionResponse = interaction?.deferPublicResponse() ?: return@execute
            val user = databaseService.users.getOrCreateUser(target, guild)
            if (user.getGuildInfo(guild.id.toString()).info.isEmpty()) {
                respond("${target.mention} has no message records.")
                return@execute
            }
            databaseService.users.removeInfo(guild, user, id)
            interactionResponse.respond { content = "Message record removed from ${target.mention}." }
        }
    }
}
