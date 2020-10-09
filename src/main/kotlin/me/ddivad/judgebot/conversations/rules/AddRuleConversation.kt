package me.ddivad.judgebot.conversations.rules

import com.gitlab.kordlib.core.entity.Guild
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.dataclasses.Rule
import me.ddivad.judgebot.embeds.createRuleEmbed
import me.ddivad.judgebot.services.DatabaseService
import me.jakejmattson.discordkt.api.arguments.BooleanArg
import me.jakejmattson.discordkt.api.arguments.EveryArg
import me.jakejmattson.discordkt.api.arguments.UrlArg
import me.jakejmattson.discordkt.api.dsl.Conversation
import me.jakejmattson.discordkt.api.dsl.conversation

class AddRuleConversation(private val configuration: Configuration,
                          private val databaseService: DatabaseService): Conversation() {
    @Conversation.Start
    fun createAddRuleConversation(guild: Guild) = conversation {
        val rules = databaseService.guilds.getRules(guild)
        val nextId = rules?.size?.plus(1)!!

        val ruleName = promptMessage(EveryArg, "Please enter rule name:")
        val ruleText = promptMessage(EveryArg, "Please enter rule text")
        val addLink = promptMessage(BooleanArg("Add link to rule?", "Y", "N"),
                "Do you want to add a link to the rule? (Y/N)")
        val ruleLink = when {
            addLink -> promptMessage(UrlArg, "Please enter the link")
            else -> ""
        }

        val newRule = Rule(nextId, ruleName, ruleText, ruleLink)
        databaseService.guilds.addRule(guild, newRule)
        respond("Rule created.")
        respond{
            createRuleEmbed(guild, newRule)
        }
    }
}