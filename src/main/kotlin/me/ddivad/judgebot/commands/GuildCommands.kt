package me.ddivad.judgebot.commands

import dev.kord.core.behavior.interaction.response.respond
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.dataclasses.GuildConfiguration
import me.ddivad.judgebot.dataclasses.LoggingConfiguration
import me.ddivad.judgebot.dataclasses.Permissions
import me.ddivad.judgebot.embeds.createConfigEmbed
import me.ddivad.judgebot.services.DatabaseService
import me.ddivad.judgebot.services.infractions.MuteService
import me.jakejmattson.discordkt.arguments.*
import me.jakejmattson.discordkt.commands.subcommand

@Suppress("unused")
fun configurationSubcommands(configuration: Configuration, databaseService: DatabaseService, muteService: MuteService) = subcommand("Configuration", Permissions.ADMINISTRATOR) {
    sub("setup", "Configure a guild to use Judgebot.") {
        execute(ChannelArg("LogChannel"), ChannelArg("AlertChannel"), RoleArg("MuteRole")) {
            val (logChannel, alertChannel, mutedRole) = args
            val interactionResponse = interaction?.deferPublicResponse() ?: return@execute
            val newConfiguration = GuildConfiguration(loggingConfiguration = LoggingConfiguration(alertChannel.id, logChannel.id), mutedRole = mutedRole.id)
            databaseService.guilds.setupGuild(guild)
            configuration.setup(guild, newConfiguration)
            muteService.setupMutedRole(guild)
            interactionResponse.respond { content = "Guild setup" }
        }
    }

    sub("role", "Add or remove configured roles") {
        execute(
            ChoiceArg("RoleType", "Role type to set", "Admin", "Staff", "Moderator"),
            ChoiceArg("Operation", "Add / Remove", "Add", "Remove"),
            RoleArg
        ) {
            val (roleType, operation, role) = args
            val guildConfiguration = configuration[guild.id] ?: return@execute

            when {
                roleType == "Admin" && operation == "Add" -> {
                    guildConfiguration.adminRoles.add(role.id)
                    respondPublic("Added **${role.name}** to Admin roles.")
                }
                roleType == "Admin" && guildConfiguration.adminRoles.contains(role.id) -> {
                    guildConfiguration.adminRoles.remove(role.id)
                    respondPublic("Removed **${role.name}** from Admin Roles")
                }
                roleType == "Staff" && operation == "Add" -> {
                    guildConfiguration.staffRoles.add(role.id)
                    respondPublic("Added **${role.name}** to Staff roles.")
                }
                roleType == "Staff" && guildConfiguration.staffRoles.contains(role.id) -> {
                    guildConfiguration.staffRoles.remove(role.id)
                    respondPublic("Removed **${role.name}** from Staff Roles")
                }
                roleType == "Moderator" && operation == "Add" -> {
                    guildConfiguration.moderatorRoles.add(role.id)
                    respondPublic("Added **${role.name}** to Moderator roles.")
                }
                roleType == "Moderator" && guildConfiguration.moderatorRoles.contains(role.id) -> {
                    guildConfiguration.moderatorRoles.remove(role.id)
                    respondPublic("Removed **${role.name}** from Moderator Roles")
                }
                else -> {
                    respond("Invalid choice.")
                }
            }
            configuration.save()
        }
    }

    sub("channel", "Set the Alert or Logging channels") {
        execute(ChoiceArg("ChannelType", "Channel type to modify", "Logging", "Alert"), ChannelArg("Channel")) {
            val (channelType, channel) = args
            val guildConfiguration = configuration[guild.id] ?: return@execute
            when (channelType) {
                "Logging" -> {
                    guildConfiguration.loggingConfiguration.loggingChannel = channel.id
                }
                "Alert" -> {
                    guildConfiguration.loggingConfiguration.alertChannel = channel.id
                }
            }
            configuration.save()
            respondPublic("Channel set to ${channel.mention}")
        }
    }

    sub("reaction", "Set the reactions used as various command shortcuts") {
        execute(
            ChoiceArg("Reaction", "Choose the reaction to set", "Flag Message", "Gag", "Delete Message"),
            UnicodeEmojiArg()
        ) {
            val (reactionType, reaction) = args
            val guildConfiguration = configuration[guild.id] ?: return@execute
            when (reactionType) {
                "Gag" -> {
                    guildConfiguration.reactions.gagReaction = reaction.unicode
                }
                "Flag Message" -> {
                    guildConfiguration.reactions.flagMessageReaction = reaction.unicode
                }
                "Delete Message" -> {
                    guildConfiguration.reactions.deleteMessageReaction = reaction.unicode
                }
            }
            configuration.save()
            respondPublic("Reaction set to ${reaction.unicode}")
        }
    }

    sub("view", "View guild configuration") {
        execute {
            val guildConfiguration = configuration[guild.id] ?: return@execute
            respondPublic {
                createConfigEmbed(guildConfiguration, guild)
            }
        }
    }
}