package me.ddivad.judgebot.conversations.rules

import dev.kord.core.entity.Guild
import me.ddivad.judgebot.services.DatabaseService
import me.jakejmattson.discordkt.arguments.IntegerArg
import me.jakejmattson.discordkt.conversations.slashConversation

class ArchiveRuleConversation(private val databaseService: DatabaseService) {
    fun createArchiveRuleConversation(guild: Guild) = slashConversation("cancel") {
        val ruleToArchive = prompt(IntegerArg, "Please enter rule number to archive:")

        databaseService.guilds.archiveRule(guild, ruleToArchive)
        respond("Rule archived.")
    }
}