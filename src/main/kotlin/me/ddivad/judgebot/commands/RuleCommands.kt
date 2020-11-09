package me.ddivad.judgebot.commands

import me.ddivad.judgebot.arguments.RuleArg
import me.ddivad.judgebot.conversations.rules.AddRuleConversation
import me.ddivad.judgebot.conversations.rules.ArchiveRuleConversation
import me.ddivad.judgebot.conversations.rules.EditRuleConversation
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.embeds.createRuleEmbed
import me.ddivad.judgebot.embeds.createRuleEmbedForStrike
import me.ddivad.judgebot.embeds.createRulesEmbed
import me.ddivad.judgebot.embeds.createRulesEmbedDetailed
import me.ddivad.judgebot.services.DatabaseService
import me.ddivad.judgebot.services.PermissionLevel
import me.ddivad.judgebot.services.requiredPermissionLevel
import me.jakejmattson.discordkt.api.dsl.commands

fun ruleCommands(configuration: Configuration,
                 databaseService: DatabaseService) = commands("Rules") {

    guildCommand("addRule") {
        description = "Add a rule to this guild."
        requiredPermissionLevel = PermissionLevel.Administrator
        execute {
            AddRuleConversation(configuration, databaseService)
                    .createAddRuleConversation(guild)
                    .startPublicly(discord, author, channel)
        }
    }

    guildCommand("editRule") {
        description = "Edit a rule in this guild."
        requiredPermissionLevel = PermissionLevel.Administrator
        execute {
            EditRuleConversation(configuration, databaseService)
                    .createAddRuleConversation(guild)
                    .startPublicly(discord, author, channel)
        }
    }

    guildCommand("archiveRule") {
        description = "Archive a rule in this guild."
        requiredPermissionLevel = PermissionLevel.Administrator
        execute {
            ArchiveRuleConversation(configuration, databaseService)
                    .createArchiveRuleConversation(guild)
                    .startPublicly(discord, author, channel)
        }
    }

    guildCommand("rules") {
        description = "List the rules of this guild."
        requiredPermissionLevel = PermissionLevel.Everyone
        execute {
            respond {
                createRulesEmbed(guild, databaseService.guilds.getRules(guild))
            }
        }
    }

    guildCommand("longRules") {
        description = "List the rules (with descriptions) of this guild."
        requiredPermissionLevel = PermissionLevel.Staff
        execute {
            respond {
                createRulesEmbedDetailed(guild, databaseService.guilds.getRules(guild)!!)
            }
        }
    }

    guildCommand("rule") {
        description = "List a rule from this guild."
        requiredPermissionLevel = PermissionLevel.Everyone
        execute(RuleArg) {
            val rule = databaseService.guilds.getRule(guild, args.first.number)!!
            respond {
                createRuleEmbed(guild, rule)
            }
        }
    }
}
