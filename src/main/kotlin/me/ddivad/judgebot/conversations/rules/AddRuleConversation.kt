package me.ddivad.judgebot.conversations.rules

import dev.kord.core.entity.Guild
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.dataclasses.Rule
import me.ddivad.judgebot.embeds.createRuleEmbed
import me.ddivad.judgebot.services.DatabaseService
import me.jakejmattson.discordkt.api.arguments.BooleanArg
import me.jakejmattson.discordkt.api.arguments.EveryArg
import me.jakejmattson.discordkt.api.arguments.UrlArg
import me.jakejmattson.discordkt.api.conversations.conversation

class AddRuleConversation(private val databaseService: DatabaseService) {
    fun createAddRuleConversation(guild: Guild) = conversation("cancel") {
        val rules = databaseService.guilds.getRules(guild)
        val nextId = rules.size.plus(1)

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