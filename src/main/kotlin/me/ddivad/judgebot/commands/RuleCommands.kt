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
import me.jakejmattson.discordkt.arguments.MessageArg
import me.jakejmattson.discordkt.commands.commands
import me.jakejmattson.discordkt.extensions.jumpLink

@Suppress("unused")
fun ruleCommands(databaseService: DatabaseService) = commands("Rule") {

    command("addRule") {
        description = "Add a rule to this guild."
        requiredPermission = Permissions.ADMINISTRATOR
        execute {
            AddRuleConversation(databaseService)
                    .createAddRuleConversation(guild)
                    .startPublicly(discord, author, channel)
        }
    }

    command("editRule") {
        description = "Edit a rule in this guild."
        requiredPermission = Permissions.ADMINISTRATOR
        execute {
            EditRuleConversation(databaseService)
                    .createAddRuleConversation(guild)
                    .startPublicly(discord, author, channel)
        }
    }

    command("archiveRule") {
        description = "Archive a rule in this guild."
        requiredPermission = Permissions.ADMINISTRATOR
        execute {
            ArchiveRuleConversation(databaseService)
                    .createArchiveRuleConversation(guild)
                    .startPublicly(discord, author, channel)
        }
    }

    command("rules") {
        description = "List the rules of this guild. Pass a message ID to edit existing rules embed."
        requiredPermission = Permissions.NONE
        execute(MessageArg.optionalNullable(null)) {
            val messageToEdit = args.first
            if (messageToEdit != null) {
                messageToEdit.edit { this.embeds?.first()?.createRulesEmbed(guild, databaseService.guilds.getRules(guild)) }
                respond("Existing embed updated: ${messageToEdit.jumpLink()}")
            } else {
                respond {
                    createRulesEmbed(guild, databaseService.guilds.getRules(guild))
                }
            }
        }
    }

    command("longRules") {
        description = "List the rules (with descriptions) of this guild. Pass a message ID to edit existing rules embed."
        requiredPermission = Permissions.STAFF
        execute(MessageArg.optionalNullable(null)) {
            val messageToEdit = args.first
            if (messageToEdit != null) {
                messageToEdit.edit { embeds?.first()?.createRulesEmbedDetailed(guild, databaseService.guilds.getRules(guild))}
                respond("Existing embed updated: ${messageToEdit.jumpLink()}")
            } else {
                respond {
                    createRulesEmbedDetailed(guild, databaseService.guilds.getRules(guild))
                }
            }
        }
    }

    command("rule") {
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
