package me.ddivad.judgebot.commands

import dev.kord.core.behavior.edit
import dev.kord.rest.builder.message.modify.embed
import me.ddivad.judgebot.arguments.autoCompletingRuleArg
import me.ddivad.judgebot.conversations.rules.AddRuleConversation
import me.ddivad.judgebot.conversations.rules.ArchiveRuleConversation
import me.ddivad.judgebot.conversations.rules.EditRuleConversation
import me.ddivad.judgebot.dataclasses.Permissions
import me.ddivad.judgebot.embeds.createRuleEmbed
import me.ddivad.judgebot.embeds.createRulesEmbed
import me.ddivad.judgebot.embeds.createRulesEmbedDetailed
import me.ddivad.judgebot.services.DatabaseService
import me.jakejmattson.discordkt.arguments.IntegerArg
import me.jakejmattson.discordkt.arguments.MessageArg
import me.jakejmattson.discordkt.commands.commands
import me.jakejmattson.discordkt.commands.subcommand
import me.jakejmattson.discordkt.extensions.jumpLink

@Suppress("unused")
fun ruleSubCommands(databaseService: DatabaseService) = subcommand("Rule", Permissions.ADMINISTRATOR) {
    sub("add", "Add a rule to this guild.") {
        execute {
            AddRuleConversation(databaseService)
                .createAddRuleConversation(guild)
                .startSlashResponse(discord, author, this)
        }
    }
    sub("edit", "Edit a rule in this guild.") {
        execute(autoCompletingRuleArg(databaseService)) {
            EditRuleConversation(databaseService)
                .createAddRuleConversation(guild, args.first)
                .startSlashResponse(discord, author, this)
        }
    }
    sub("archive", "Archive a rule in this guild.") {
        execute {
            ArchiveRuleConversation(databaseService)
                .createArchiveRuleConversation(guild)
                .startSlashResponse(discord, author, this)
        }
    }
}

@Suppress("unused")
fun ruleCommands(databaseService: DatabaseService) = commands("Rules") {
    slash(
        "rules",
        "List the rules of this guild. Pass a message ID to edit existing rules embed.",
        Permissions.EVERYONE
    ) {
        execute(MessageArg.optionalNullable(null)) {
            val messageToEdit = args.first
            if (messageToEdit != null) {
                messageToEdit.edit {
                    this.embeds?.first()?.createRulesEmbed(guild, databaseService.guilds.getRules(guild))
                }
                respond("Existing embed updated: ${messageToEdit.jumpLink()}")
            } else {
                respondPublic {
                    createRulesEmbed(guild, databaseService.guilds.getRules(guild))
                }
            }
        }
    }

    slash(
        "longRules",
        "List the rules (with descriptions) of this guild. Pass a message ID to edit existing rules embed.",
        Permissions.STAFF
    ) {
        execute(MessageArg.optionalNullable(null)) {
            val messageToEdit = args.first
            if (messageToEdit != null) {
                messageToEdit.edit {
                    embed { createRulesEmbedDetailed(guild, databaseService.guilds.getRules(guild)) }
                }
                respondPublic("Existing embed updated: ${messageToEdit.jumpLink()}")
            } else {
                respondPublic {
                    createRulesEmbedDetailed(guild, databaseService.guilds.getRules(guild))
                }
            }
        }
    }

    slash("viewRule", "List a rule from this guild.", Permissions.EVERYONE) {
        execute(IntegerArg) {
            val rule = databaseService.guilds.getRule(guild, args.first)!!
            respondPublic {
                createRuleEmbed(guild, rule)
            }
        }
    }
}
