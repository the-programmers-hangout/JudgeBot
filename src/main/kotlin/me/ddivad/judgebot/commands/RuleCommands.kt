package me.ddivad.judgebot.commands

import dev.kord.core.behavior.edit
import me.ddivad.judgebot.arguments.RuleArg
import me.ddivad.judgebot.conversations.rules.AddRuleConversation
import me.ddivad.judgebot.conversations.rules.ArchiveRuleConversation
import me.ddivad.judgebot.conversations.rules.EditRuleConversation
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.dataclasses.Permissions
import me.ddivad.judgebot.embeds.createRuleEmbed
import me.ddivad.judgebot.embeds.createRulesEmbed
import me.ddivad.judgebot.embeds.createRulesEmbedDetailed
import me.ddivad.judgebot.services.DatabaseService
import me.jakejmattson.discordkt.api.arguments.MessageArg
import me.jakejmattson.discordkt.api.dsl.commands
import me.jakejmattson.discordkt.api.extensions.jumpLink

@Suppress("unused")
fun ruleCommands(configuration: Configuration,
                 databaseService: DatabaseService) = commands("Rule") {

    guildCommand("addRule") {
        description = "Add a rule to this guild."
        requiredPermission = Permissions.ADMINISTRATOR
        execute {
            AddRuleConversation(databaseService)
                    .createAddRuleConversation(guild)
                    .startPublicly(discord, author, channel)
        }
    }

    guildCommand("editRule") {
        description = "Edit a rule in this guild."
        requiredPermission = Permissions.ADMINISTRATOR
        execute {
            EditRuleConversation(databaseService)
                    .createAddRuleConversation(guild)
                    .startPublicly(discord, author, channel)
        }
    }

    guildCommand("archiveRule") {
        description = "Archive a rule in this guild."
        requiredPermission = Permissions.ADMINISTRATOR
        execute {
            ArchiveRuleConversation(databaseService)
                    .createArchiveRuleConversation(guild)
                    .startPublicly(discord, author, channel)
        }
    }

    guildCommand("rules") {
        description = "List the rules of this guild. Pass a message ID to edit existing rules embed."
        requiredPermission = Permissions.NONE
        execute(MessageArg.optionalNullable(null)) {
            val messageToEdit = args.first
            if (messageToEdit != null) {
                messageToEdit.edit { this.embed { createRulesEmbed(guild, databaseService.guilds.getRules(guild)) } }
                respond("Existing embed updated: ${messageToEdit.jumpLink()}")
            } else {
                respond {
                    createRulesEmbed(guild, databaseService.guilds.getRules(guild))
                }
            }
        }
    }

    guildCommand("longRules") {
        description = "List the rules (with descriptions) of this guild. Pass a message ID to edit existing rules embed."
        requiredPermission = Permissions.STAFF
        execute(MessageArg.optionalNullable(null)) {
            val messageToEdit = args.first
            if (messageToEdit != null) {
                messageToEdit.edit { this.embed { createRulesEmbedDetailed(guild, databaseService.guilds.getRules(guild)) } }
                respond("Existing embed updated: ${messageToEdit.jumpLink()}")
            } else {
                respond {
                    createRulesEmbedDetailed(guild, databaseService.guilds.getRules(guild))
                }
            }
        }
    }

    guildCommand("rule") {
        description = "List a rule from this guild."
        requiredPermission = Permissions.NONE
        execute(RuleArg) {
            val rule = databaseService.guilds.getRule(guild, args.first.number)!!
            respond {
                createRuleEmbed(guild, rule)
            }
        }
    }
}
