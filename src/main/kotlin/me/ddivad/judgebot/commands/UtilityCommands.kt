package me.ddivad.judgebot.commands

import me.ddivad.judgebot.services.HelpService
import me.jakejmattson.discordkt.api.arguments.AnyArg
import me.jakejmattson.discordkt.api.arguments.CommandArg
import me.jakejmattson.discordkt.api.dsl.commands

@Suppress("unused")
fun createInformationCommands(helpService: HelpService) = commands("Utility") {
    command("help") {
        description = "Display help information."
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
}