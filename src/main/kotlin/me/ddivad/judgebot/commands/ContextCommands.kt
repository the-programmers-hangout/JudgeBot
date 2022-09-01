package me.ddivad.judgebot.commands

import dev.kord.core.behavior.getChannelOf
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.channel.TextChannel
import dev.kord.x.emoji.Emojis
import dev.kord.x.emoji.addReaction
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.dataclasses.Permissions
import me.ddivad.judgebot.embeds.createCondensedHistoryEmbed
import me.ddivad.judgebot.embeds.createSelfHistoryEmbed
import me.ddivad.judgebot.extensions.hasStaffRoles
import me.ddivad.judgebot.services.DatabaseService
import me.ddivad.judgebot.services.infractions.BadPfpService
import me.ddivad.judgebot.util.createFlagMessage
import me.jakejmattson.discordkt.commands.commands

@Suppress("Unused")
fun contextCommands(configuration: Configuration, databaseService: DatabaseService, badPfpService: BadPfpService) =
    commands("Context") {
        message(
            "Report Message",
            "report",
            "Report a message to staff (please use via the 'Apps' menu instead of as a command)"
        ) {
            val guildConfiguration = configuration[guild.asGuild().id] ?: return@message
            guild.getChannelOf<TextChannel>(guildConfiguration.loggingConfiguration.alertChannel)
                .createMessage(createFlagMessage(author, args.first, channel))
                .addReaction(Emojis.question)
            respond("Message flagged successfully, thanks!")
        }

        user(
            "History",
            "contextUserHistory",
            "View a condensed history for this user (please use via the 'Apps' menu instead of as a command)",
            Permissions.EVERYONE
        ) {
            val targetMember = arg.asMemberOrNull(guild.id) ?: return@user
            val guildConfiguration = configuration[guild.id] ?: return@user
            if (author.asMember(guild.id).hasStaffRoles(guildConfiguration)) {
                respond("Record for ${arg.mention}") {
                    createCondensedHistoryEmbed(
                        arg,
                        databaseService.users.getOrCreateUser(arg, guild.asGuild()),
                        guild.asGuild(),
                        configuration
                    )
                }
            } else if (author.id == targetMember.id) {
                respond {
                    createSelfHistoryEmbed(
                        arg,
                        databaseService.users.getOrCreateUser(arg, guild.asGuild()),
                        guild.asGuild(),
                        configuration
                    )
                }
            } else respond("Missing required permissions")
        }

        user(
            "BadPFP",
            "contextUserBadpfp",
            "Apply a badpfp to a user (please use via the 'Apps' menu instead of as a command)",
            Permissions.STAFF
        ) {
            val targetMember = arg.asMemberOrNull(guild.id)
            val interactionResponse = interaction!!.deferEphemeralResponse()
            if (targetMember == null) {
                respond("Member ${arg.mention} is no longer in this guild")
                return@user
            }
            badPfpService.applyBadPfp(targetMember, guild)
            interactionResponse.respond {
                content = "${targetMember.mention} has been muted and a badpfp has been triggered."
            }
        }
    }
