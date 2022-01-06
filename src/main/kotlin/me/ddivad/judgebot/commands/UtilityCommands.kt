package me.ddivad.judgebot.commands

import dev.kord.core.behavior.getChannelOf
import dev.kord.core.entity.channel.TextChannel
import dev.kord.x.emoji.Emojis
import dev.kord.x.emoji.addReaction
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.dataclasses.Permissions
import me.ddivad.judgebot.services.HelpService
import me.ddivad.judgebot.util.createFlagMessage
import me.jakejmattson.discordkt.arguments.AnyArg
import me.jakejmattson.discordkt.arguments.MessageArg
import me.jakejmattson.discordkt.commands.commands
import me.jakejmattson.discordkt.extensions.toSnowflake

@Suppress("unused")
fun createInformationCommands(helpService: HelpService, configuration: Configuration) = commands("Utility") {
    command("help") {
        description = "Display help information."
        requiredPermission = Permissions.NONE
        execute(AnyArg("Command").optional("")) {
            val input = args.first
            if (input == "") {
                helpService.buildHelpEmbed(this)
            } else {
                val cmd = discord.commands.find { command ->
                    command.names.any { it.equals(input, ignoreCase = true) }
                } ?: return@execute
                helpService.sendHelpEmbed(this, cmd)
            }
        }
    }

    slash("report", "Report Message") {
        description = "Flag a message for moderators to review."
        requiredPermission = Permissions.NONE
        execute(MessageArg) {
            val guild = guild ?: return@execute
            val guildConfiguration = configuration[guild.asGuild().id.value] ?: return@execute

            guild.getChannelOf<TextChannel>(guildConfiguration.loggingConfiguration.alertChannel.toSnowflake())
                .createMessage(createFlagMessage(author, args.first, channel))
                .addReaction(Emojis.question)

            respond("Message flagged successfully, thanks!")
        }
    }
}