package me.ddivad.judgebot.commands

import me.ddivad.judgebot.conversations.ConfigurationConversation
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.embeds.createConfigEmbed
import me.ddivad.judgebot.services.DatabaseService
import me.ddivad.judgebot.services.PermissionLevel
import me.ddivad.judgebot.services.requiredPermissionLevel
import me.jakejmattson.discordkt.api.arguments.ChannelArg
import me.jakejmattson.discordkt.api.arguments.EveryArg
import me.jakejmattson.discordkt.api.arguments.RoleArg
import me.jakejmattson.discordkt.api.dsl.commands

fun guildConfigCommands(configuration: Configuration,
                        databaseService: DatabaseService) = commands("Configuration") {
    guildCommand("configure") {
        description = "Configure a guild to use Judgebot."
        requiredPermissionLevel = PermissionLevel.Administrator
        execute {
            if (configuration.hasGuildConfig(guild.id.longValue)) {
                respond("Guild configuration exists. To modify it use the commands to set values.")
                return@execute
            }
            ConfigurationConversation(configuration)
                    .createConfigurationConversation(guild)
                    .startPublicly(discord, author, channel)
            databaseService.guilds.setupGuild(guild)
            respond("Guild setup")
        }
    }

    guildCommand("viewconfig") {
        description = "View the configuration vales for this guild."
        requiredPermissionLevel = PermissionLevel.Staff
        execute {
            if (!configuration.hasGuildConfig(guild.id.longValue)) {
                respond("Please run the **configure** command to set this initially.")
                return@execute
            }
            val config = configuration[guild.id.longValue] ?: return@execute
            respond {
                createConfigEmbed(config, guild)
            }
        }
    }

    guildCommand("setprefix") {
        description = "Set the bot prefix."
        requiredPermissionLevel = PermissionLevel.Administrator
        execute(EveryArg) {
            if (!configuration.hasGuildConfig(guild.id.longValue)) {
                respond("Please run the **configure** command to set this initially.")
                return@execute
            }
            val prefix = args.first
            configuration[guild.id.longValue]?.prefix = prefix
            configuration.save()
            respond("Prefix set to: **$prefix**")
        }
    }

    guildCommand("setstaffrole") {
        description = "Set the bot staff role."
        requiredPermissionLevel = PermissionLevel.Administrator
        execute(RoleArg) {
            if (!configuration.hasGuildConfig(guild.id.longValue)) {
                respond("Please run the **configure** command to set this initially.")
                return@execute
            }
            val role = args.first
            configuration[guild.id.longValue]?.staffRole = role.id.value
            configuration.save()
            respond("Role set to: **${role.name}**")
        }
    }

    guildCommand("setadminrole") {
        description = "Set the bot admin role."
        requiredPermissionLevel = PermissionLevel.Administrator
        execute(RoleArg) {
            if (!configuration.hasGuildConfig(guild.id.longValue)) {
                respond("Please run the **configure** command to set this initially.")
                return@execute
            }
            val role = args.first
            configuration[guild.id.longValue]?.adminRole = role.id.value
            configuration.save()
            respond("Role set to: **${role.name}**")
        }
    }

    guildCommand("setlogchannel") {
        description = "Set the channel that the bot logs will be sent."
        requiredPermissionLevel = PermissionLevel.Administrator
        execute(ChannelArg) {
            if (!configuration.hasGuildConfig(guild.id.longValue)) {
                respond("Please run the **configure** command to set this initially.")
                return@execute
            }
            val channel = args.first
            configuration[guild.id.longValue]?.loggingConfiguration?.loggingChannel = channel.id.value
            configuration.save()
            respond("Channel set to: **${channel.name}**")
        }
    }

    guildCommand("setalertchannel") {
        description = "Set the channel that the bot alerts will be sent."
        requiredPermissionLevel = PermissionLevel.Administrator
        execute(ChannelArg) {
            if (!configuration.hasGuildConfig(guild.id.longValue)) {
                respond("Please run the **configure** command to set this initially.")
                return@execute
            }
            val channel = args.first
            configuration[guild.id.longValue]?.loggingConfiguration?.alertChannel = channel.id.value
            configuration.save()
            respond("Channel set to: **${channel.name}**")
        }
    }

    guildCommand("setmuterole") {
        description = "Set the role to be used to mute members."
        requiredPermissionLevel = PermissionLevel.Administrator
        execute(RoleArg) {
            if (!configuration.hasGuildConfig(guild.id.longValue)) {
                respond("Please run the **configure** command to set this initially.")
                return@execute
            }
            val role = args.first
            configuration[guild.id.longValue]?.mutedRole = role.id.value
            configuration.save()
            respond("Role set to: **${role.name}**")
        }
    }
}