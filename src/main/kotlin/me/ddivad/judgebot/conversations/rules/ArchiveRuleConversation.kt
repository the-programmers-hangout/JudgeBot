package me.ddivad.judgebot.conversations.rules

import com.gitlab.kordlib.core.entity.Guild
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.dataclasses.Rule
import me.ddivad.judgebot.services.DatabaseService
import me.jakejmattson.discordkt.api.arguments.BooleanArg
import me.jakejmattson.discordkt.api.arguments.EveryArg
import me.jakejmattson.discordkt.api.arguments.IntegerArg
import me.jakejmattson.discordkt.api.arguments.UrlArg
import me.jakejmattson.discordkt.api.dsl.Conversation
import me.jakejmattson.discordkt.api.dsl.conversation

class ArchiveRuleConversation(private val configuration: Configuration,
                          private val databaseService: DatabaseService): Conversation() {
    @Conversation.Start
    fun createArchiveRuleConversation(guild: Guild) = conversation {
        val ruleToArchive = promptMessage(IntegerArg, "Please enter rule number to archive:")

        databaseService.guilds.archiveRule(guild, ruleToArchive)
        respond("Rule archived.")
    }
}