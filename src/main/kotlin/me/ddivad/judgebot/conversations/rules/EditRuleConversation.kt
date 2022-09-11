package me.ddivad.judgebot.conversations.rules

import dev.kord.core.entity.Guild
import me.ddivad.judgebot.dataclasses.Rule
import me.ddivad.judgebot.embeds.createRuleEmbed
import me.ddivad.judgebot.services.DatabaseService
import me.jakejmattson.discordkt.arguments.BooleanArg
import me.jakejmattson.discordkt.arguments.EveryArg
import me.jakejmattson.discordkt.arguments.IntegerArg
import me.jakejmattson.discordkt.arguments.UrlArg
import me.jakejmattson.discordkt.conversations.slashConversation

class EditRuleConversation(private val databaseService: DatabaseService) {
    fun createAddRuleConversation(guild: Guild, ruleName: String) = slashConversation("cancel") {
        val rules = databaseService.guilds.getRulesForInfractionPrompt(guild)
        val ruleToUpdate = rules.find { it.number == ruleName.split(" -").first().toInt() } ?: return@slashConversation
        respond("Current Rule:")
        respond {
            createRuleEmbed(guild, ruleToUpdate)
        }

        val updateNumber = prompt(
            BooleanArg(truthValue = "y", falseValue = "n"),
            "Update Rule number? (Y/N)"
        )
        val ruleNumber = when {
            updateNumber -> promptUntil(
                argument = IntegerArg,
                prompt = "Please enter rule number:",
                isValid = { number -> !rules.any { it.number == number } },
                error = "Rule with that number already exists"
            )
            else -> ruleToUpdate.number
        }
        val updateName = prompt(
            BooleanArg(truthValue = "y", falseValue = "n"),
            "Update Rule name? (Y/N)"
        )
        val ruleName = when {
            updateName -> promptUntil(
                EveryArg,
                "Please enter rule name:",
                "Rule with that name already exists",
                isValid = { name -> !rules.any { it.title == name } }
            )
            else -> ruleToUpdate.title
        }

        val updateText = prompt(
            BooleanArg(truthValue = "y", falseValue = "n"),
            "Update Rule text? (Y/N)"
        )
        val ruleText = when {
            updateText -> prompt(EveryArg, "Please enter rule text:")
            else -> ruleToUpdate.description
        }

        val updateLink = prompt(
            BooleanArg(truthValue = "y", falseValue = "n"),
            "Update Rule link? (Y/N)"
        )
        val ruleLink = when {
            updateLink -> prompt(UrlArg, "Please enter the link")
            else -> ruleToUpdate.link
        }

        val newRule = Rule(ruleNumber, ruleName, ruleText, ruleLink, ruleToUpdate.archived)
        databaseService.guilds.editRule(guild, ruleToUpdate, newRule)
        respond("Rule edited.")
        respond {
            createRuleEmbed(guild, newRule)
        }
    }
}