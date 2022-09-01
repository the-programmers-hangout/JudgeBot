package me.ddivad.judgebot.commands

import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.dataclasses.Permissions
import me.ddivad.judgebot.embeds.createSelfHistoryEmbed
import me.ddivad.judgebot.services.DatabaseService
import me.jakejmattson.discordkt.commands.commands

@Suppress("unused")
fun createInformationCommands(
    configuration: Configuration,
    databaseService: DatabaseService,
) = commands("Utility") {
    slash("selfHistory", "View your infraction history.", Permissions.EVERYONE) {
        execute {
            val user = author
            val guildMember = databaseService.users.getOrCreateUser(user, guild)
            respond { createSelfHistoryEmbed(user, guildMember, guild, configuration) }
        }
    }
}