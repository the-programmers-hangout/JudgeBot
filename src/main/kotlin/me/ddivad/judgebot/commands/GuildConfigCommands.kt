package me.ddivad.judgebot.commands

import me.ddivad.judgebot.arguments.GuildConfigArg
import me.ddivad.judgebot.conversations.guild.GuildSetupConversation
import me.ddivad.judgebot.conversations.guild.EditConfigConversation
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.services.DatabaseService
import me.ddivad.judgebot.services.MuteService
import me.ddivad.judgebot.services.PermissionLevel
import me.ddivad.judgebot.services.requiredPermissionLevel
import me.jakejmattson.discordkt.api.dsl.commands

fun guildConfigCommands(configuration: Configuration,
                        databaseService: DatabaseService,
                        muteService: MuteService) = commands("Configuration") {
    guildCommand("setup") {
        description = "Configure a guild to use Judgebot."
        requiredPermissionLevel = PermissionLevel.Administrator
        execute {
            if (configuration.hasGuildConfig(guild.id.longValue)) {
                respond("Guild configuration exists. To modify it use the commands to set values.")
                return@execute
            }
            GuildSetupConversation(configuration, muteService)
                    .createSetupConversation(guild)
                    .startPublicly(discord, author, channel)
            databaseService.guilds.setupGuild(guild)
            respond("Guild setup")
        }
    }

    guildCommand("configuration") {
        description = "Update configuration parameters for this guild (conversation)."
        requiredPermissionLevel = PermissionLevel.Staff
        execute(GuildConfigArg) {
            if (!configuration.hasGuildConfig(guild.id.longValue)) {
                respond("Please run the **setup** command to set this initially.")
                return@execute
            }
            EditConfigConversation(configuration)
                    .createEditConfigurationConversation(guild, args.first)
                    .startPublicly(discord, author, channel)
        }
    }
}