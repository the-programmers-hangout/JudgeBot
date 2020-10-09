package me.ddivad.judgebot.commands

import me.ddivad.judgebot.conversations.rules.AddRuleConversation
import me.ddivad.judgebot.conversations.rules.ArchiveRuleConversation
import me.ddivad.judgebot.conversations.rules.EditRuleConversation
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.embeds.createRuleEmbed
import me.ddivad.judgebot.embeds.createRulesEmbed
import me.ddivad.judgebot.embeds.createRulesEmbedDetailed
import me.ddivad.judgebot.services.DatabaseService
import me.ddivad.judgebot.services.PermissionLevel
import me.ddivad.judgebot.services.requiredPermissionLevel
import me.jakejmattson.discordkt.api.arguments.IntegerArg
import me.jakejmattson.discordkt.api.dsl.commands
import me.jakejmattson.discordkt.api.services.ConversationService

fun ruleCommands(configuration: Configuration,
                        conversationService: ConversationService,
                        databaseService: DatabaseService) = commands("Rules") {

    command("addRule") {
        description = "Add a rule to this guild."
        requiredPermissionLevel = PermissionLevel.Administrator
        execute {
            conversationService.startPublicConversation<AddRuleConversation>(author, channel.asChannel(), guild!!)
        }
    }

    command("editRule") {
        description = "Edit a rule in this guild."
        requiredPermissionLevel = PermissionLevel.Administrator
        execute {
            conversationService.startPublicConversation<EditRuleConversation>(author, channel.asChannel(), guild!!)
        }
    }

    command("archiveRule") {
        description = "Archive a rule in this guild."
        requiredPermissionLevel = PermissionLevel.Administrator
        execute {
            conversationService.startPublicConversation<ArchiveRuleConversation>(author, channel.asChannel(), guild!!)
        }
    }

    command("ruleHeadings") {
        description = "List the rules of this guild."
        requiredPermissionLevel = PermissionLevel.Everyone
        execute {
            respond {
                createRulesEmbed(guild!!, databaseService.guilds.getRules(guild!!)!!)
            }
        }
    }

    command("listRules") {
        description = "List the rules of this guild."
        requiredPermissionLevel = PermissionLevel.Everyone
        execute {
            respond {
                createRulesEmbedDetailed(guild!!, databaseService.guilds.getRules(guild!!)!!)
            }
        }
    }

    command("rule") {
        description = "List a rule from this guild."
        requiredPermissionLevel = PermissionLevel.Everyone
        execute(IntegerArg) {
            val rule = databaseService.guilds.getRule(guild!!, args.first)!!
            respond {
                createRuleEmbed(guild!!, rule)
            }
        }
    }
}
