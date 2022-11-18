package me.ddivad.judgebot.commands

import dev.kord.common.annotation.KordPreview
import dev.kord.common.exception.RequestException
import dev.kord.core.behavior.interaction.response.respond
import me.ddivad.judgebot.arguments.LowerMemberArg
import me.ddivad.judgebot.arguments.autoCompletingRuleArg
import me.ddivad.judgebot.arguments.autoCompletingWeightArg
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.dataclasses.Infraction
import me.ddivad.judgebot.dataclasses.InfractionType
import me.ddivad.judgebot.dataclasses.Permissions
import me.ddivad.judgebot.embeds.createHistoryEmbed
import me.ddivad.judgebot.extensions.testDmStatus
import me.ddivad.judgebot.services.DatabaseService
import me.ddivad.judgebot.services.infractions.BadPfpService
import me.ddivad.judgebot.services.infractions.InfractionService
import me.jakejmattson.discordkt.arguments.AnyArg
import me.jakejmattson.discordkt.arguments.BooleanArg
import me.jakejmattson.discordkt.arguments.ChoiceArg
import me.jakejmattson.discordkt.arguments.EveryArg
import me.jakejmattson.discordkt.commands.commands
import me.jakejmattson.discordkt.extensions.createMenu

@KordPreview
@Suppress("unused")
fun createInfractionCommands(
    databaseService: DatabaseService,
    config: Configuration,
    infractionService: InfractionService,
    badPfpService: BadPfpService
) = commands("Infraction") {
    slash("warn", "Warn a user.", Permissions.MODERATOR) {
        execute(
            LowerMemberArg("Member", "Target Member"),
            autoCompletingRuleArg(databaseService),
            AnyArg("Reason", "Infraction reason to send to user"),
            BooleanArg(
                "Force",
                "true",
                "false",
                "Override recommendation to strike user instead. Please discuss with staff before using this option"
            ).optional(false)
        ) {
            val (targetMember, ruleName, reason, force) = args
            val guildConfiguration = config[guild.id] ?: return@execute
            val interactionResponse = interaction?.deferPublicResponse() ?: return@execute
            val user = databaseService.users.getOrCreateUser(targetMember, guild)
            if (user.getTotalHistoricalPoints(guild) >= guildConfiguration.infractionConfiguration.warnUpgradeThreshold && !force) {
                interactionResponse.respond {
                    content =
                        "This user has more than ${guildConfiguration.infractionConfiguration.warnUpgradeThreshold} historical points, so please consider striking (`/strike`) instead. You can use the optional `Force` argument to override this, but please discuss with other staff before doing so."
                }
                return@execute
            }
            val infraction = Infraction(
                author.id.toString(),
                reason,
                InfractionType.Warn,
                guildConfiguration.infractionConfiguration.warnPoints,
                getRuleNumber(ruleName)
            )
            infractionService.infract(targetMember, guild, user, infraction)
            val dmEnabled: Boolean = try {
                targetMember.testDmStatus()
                true
            } catch (ex: RequestException) {
                false
            }
            channel.createMenu { createHistoryEmbed(targetMember, user, guild, config, databaseService) }
            interactionResponse.respond {
                content =
                    "Updated history for ${targetMember.mention}: ${if (!dmEnabled) "\n**Note**: User has DMs disabled" else ""}"
            }
        }
    }

    slash("strike", "Strike a user.", Permissions.STAFF) {
        execute(
            LowerMemberArg("Member", "Target Member"),
            autoCompletingRuleArg(databaseService),
            AnyArg("Reason", "Infraction reason to send to user"),
            autoCompletingWeightArg(config)
        ) {
            val (targetMember, ruleName, reason, weight) = args
            val guildConfiguration = config[guild.id] ?: return@execute
            val interactionResponse = interaction?.deferPublicResponse() ?: return@execute
            val user = databaseService.users.getOrCreateUser(targetMember, guild)
            val infraction = Infraction(
                author.id.toString(),
                reason,
                InfractionType.Strike,
                weight.toInt() * guildConfiguration.infractionConfiguration.strikePoints,
                getRuleNumber(ruleName)
            )
            infractionService.infract(targetMember, guild, user, infraction)
            val dmEnabled: Boolean = try {
                targetMember.testDmStatus()
                true
            } catch (ex: RequestException) {
                false
            }
            channel.createMenu { createHistoryEmbed(targetMember, user, guild, config, databaseService) }
            interactionResponse.respond {
                content =
                    "Updated history for ${targetMember.mention}: ${if (!dmEnabled) "\n**Note**: User has DMs disabled" else ""}"
            }
        }
    }

    slash("badpfp", "Mutes a user and prompts them to change their pfp with a 30 minute ban timer", Permissions.STAFF) {
        execute(
            ChoiceArg("Option", "Trigger or cancel badpfp for a user", "Trigger", "Cancel"),
            LowerMemberArg("Member", "Target Member")
        ) {
            val (option, targetMember) = args
            val interactionResponse = interaction!!.deferPublicResponse()
            var dmEnabled: Boolean
            try {
                targetMember.testDmStatus()
                dmEnabled = true
            } catch (ex: RequestException) {
                dmEnabled = false
                interactionResponse.respond {
                    content = "${targetMember.mention} has DMs disabled. No messages will be sent."
                }
            }
            if (option == "Cancel") {
                when (badPfpService.hasActiveBapPfp(targetMember)) {
                    true -> {
                        badPfpService.cancelBadPfp(guild, targetMember)
                        interactionResponse.respond { content = "Badpfp cancelled for ${targetMember.mention}" }
                    }
                    false -> interactionResponse.respond {
                        content = "${targetMember.mention} does not have an active badpfp."
                    }
                }
                return@execute
            }

            val badPfp = Infraction(author.id.toString(), "BadPfp", InfractionType.BadPfp)
            badPfpService.applyBadPfp(targetMember, guild)
            databaseService.users.addInfraction(
                guild,
                databaseService.users.getOrCreateUser(targetMember, guild),
                badPfp
            )
            interactionResponse.respond {
                content = "${targetMember.mention} has been muted and a badpfp has been triggered."
            }
        }
    }

    slash("badname", "Rename a guild member that has a bad name.", Permissions.MODERATOR) {
        execute(LowerMemberArg("Member", "Target Member")) {
            infractionService.badName(args.first)
            respond("User renamed to ${args.first.mention}")
        }
    }
}

private fun getRuleNumber(ruleName: String): Int? {
    return if (ruleName.split(" -").first().toInt() > 0) {
        ruleName.split(" -").first().toInt()
    } else null
}
