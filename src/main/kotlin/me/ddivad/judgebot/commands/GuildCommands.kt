package me.ddivad.judgebot.commands

import me.ddivad.judgebot.arguments.GuildConfigArg
import me.ddivad.judgebot.conversations.guild.GuildSetupConversation
import me.ddivad.judgebot.conversations.guild.EditConfigConversation
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.dataclasses.Permissions
import me.ddivad.judgebot.embeds.createActivePunishmentsEmbed
import me.ddivad.judgebot.services.DatabaseService
import me.ddivad.judgebot.services.infractions.MuteService
import me.jakejmattson.discordkt.api.dsl.commands

fun guildConfigCommands(configuration: Configuration,
                        databaseService: DatabaseService,
                        muteService: MuteService) = commands("Guild") {
    guildCommand("setup") {
        description = "Configure a guild to use Judgebot."
        requiredPermission = Permissions.ADMINISTRATOR
        execute {
            if (configuration.hasGuildConfig(guild.id.value)) {
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
        requiredPermission = Permissions.STAFF
        execute(GuildConfigArg.optional("options")) {
            if (!configuration.hasGuildConfig(guild.id.value)) {
                respond("Please run the **setup** command to set this initially.")
                return@execute
            }
            EditConfigConversation(configuration)
                    .createEditConfigurationConversation(guild, args.first)
                    .startPublicly(discord, author, channel)
        }
    }

    guildCommand("activePunishments") {
        description = "View active punishments for a guild."
        requiredPermission = Permissions.STAFF
        execute {
            val punishments = databaseService.guilds.getActivePunishments(guild)
            if (punishments.isEmpty()) {
                respond("No active punishments found.")
                return@execute
            }
            respond { createActivePunishmentsEmbed(guild, punishments) }
        }
    }
}