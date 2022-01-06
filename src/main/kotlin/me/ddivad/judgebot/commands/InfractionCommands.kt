package me.ddivad.judgebot.commands

import dev.kord.common.annotation.KordPreview
import me.ddivad.judgebot.arguments.LowerUserArg
import dev.kord.common.exception.RequestException
import dev.kord.core.behavior.reply
import dev.kord.x.emoji.Emojis
import dev.kord.x.emoji.addReaction
import me.ddivad.judgebot.arguments.LowerMemberArg
import me.ddivad.judgebot.conversations.InfractionConversation
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.dataclasses.Infraction
import me.ddivad.judgebot.dataclasses.InfractionType
import me.ddivad.judgebot.dataclasses.Permissions
import me.ddivad.judgebot.extensions.testDmStatus
import me.ddivad.judgebot.services.*
import me.ddivad.judgebot.services.infractions.BadPfpService
import me.ddivad.judgebot.services.infractions.BadnameService
import me.ddivad.judgebot.services.infractions.InfractionService
import me.jakejmattson.discordkt.arguments.BooleanArg
import me.jakejmattson.discordkt.arguments.EveryArg
import me.jakejmattson.discordkt.arguments.IntegerArg
import me.jakejmattson.discordkt.commands.commands
import me.jakejmattson.discordkt.conversations.ConversationResult

@KordPreview
@Suppress("unused")
fun createInfractionCommands(databaseService: DatabaseService,
                             config: Configuration,
                             infractionService: InfractionService,
                             badPfpService: BadPfpService,
                             badnameService: BadnameService) = commands("Infraction") {
    command("strike", "s", "S") {
        description = "Strike a user."
        requiredPermission = Permissions.STAFF
        execute(LowerMemberArg, IntegerArg("Weight").optional(1), EveryArg("Reason")) {
            val (targetMember, weight, reason) = args
            val guildConfiguration = config[guild.id.value] ?: return@execute
            val maxStrikes = guildConfiguration.infractionConfiguration.pointCeiling / 10
            if (weight > maxStrikes) {
                respond("Maximum strike weight is **$maxStrikes (${guildConfiguration.infractionConfiguration.pointCeiling} points)**")
                return@execute
            }
            try {
                targetMember.testDmStatus()
                this.message.addReaction(Emojis.whiteCheckMark)
            } catch (ex: RequestException) {
                this.message.addReaction(Emojis.x)
                respond("${targetMember.mention} has DMs disabled. No messages will be sent.")
            }
            val conversationResult = InfractionConversation(databaseService, config, infractionService)
                    .createInfractionConversation(guild, targetMember, weight, reason, InfractionType.Strike)
                    .startPublicly(discord, author, channel)
            if (conversationResult == ConversationResult.HAS_CONVERSATION) {
                message.reply { content = "You already have an active Strike conversation. Make sure you selected a rule." }
            } else if (conversationResult == ConversationResult.EXITED) {
                message.reply { content = "Infraction cancelled." }
            }
        }
    }

    command("warn", "w", "W") {
        description = "Warn a user."
        requiredPermission = Permissions.MODERATOR
        execute(LowerMemberArg, EveryArg("Reason")) {
            val (targetMember, reason) = args
            try {
                targetMember.testDmStatus()
                this.message.addReaction(Emojis.whiteCheckMark)
            } catch (ex: RequestException) {
                this.message.addReaction(Emojis.x)
                respond("${targetMember.mention} has DMs disabled. No messages will be sent.")
            }
            InfractionConversation(databaseService, config, infractionService)
                    .createInfractionConversation(guild, targetMember, 1, reason, InfractionType.Warn)
                    .startPublicly(discord, author, channel)
        }
    }

    command("badpfp") {
        description = "Notifies the user that they should change their profile pic and applies a 30 minute mute. Bans the user if they don't change picture."
        requiredPermission = Permissions.STAFF
        execute(BooleanArg("cancel", "apply", "cancel").optional(true), LowerMemberArg) {
            val (cancel, targetMember) = args
            try {
                targetMember.testDmStatus()
                this.message.addReaction(Emojis.whiteCheckMark)
            } catch (ex: RequestException) {
                this.message.addReaction(Emojis.x)
                respond("${targetMember.mention} has DMs disabled. No messages will be sent.")
            }
            val minutesUntilBan = 30L
            val timeLimit = 1000 * 60 * minutesUntilBan
            if (!cancel) {
                when (badPfpService.hasActiveBapPfp(targetMember)) {
                    true -> {
                        badPfpService.cancelBadPfp(guild, targetMember)
                        respond("Badpfp cancelled for ${targetMember.mention}")
                    }
                    false -> respond("${targetMember.mention} does not have a an active badpfp.")
                }
                return@execute
            }

            val badPfp = Infraction(author.id.toString(), "BadPfp", InfractionType.BadPfp)
            badPfpService.applyBadPfp(targetMember, guild, timeLimit)
            respond("${targetMember.mention} has been muted and a badpfp has been triggered with a time limit of $minutesUntilBan minutes.")
        }
    }

    command("badname") {
        description = "Rename a guild member that has a bad name."
        requiredPermission = Permissions.MODERATOR
        execute(LowerMemberArg) {
            badnameService.chooseRandomNickname(args.first)
            respond("User renamed to ${args.first.mention}")
        }
    }

    command("cleanseInfractions") {
        description = "Use this to delete (permanently) as user's infractions."
        requiredPermission = Permissions.ADMINISTRATOR
        execute(LowerUserArg) {
            val user = databaseService.users.getOrCreateUser(args.first, guild)
            if (user.getGuildInfo(guild.id.toString()).infractions.isEmpty()) {
                respond("User has no infractions.")
                return@execute
            }
            databaseService.users.cleanseInfractions(guild, user)
            respond("Infractions cleansed.")
        }
    }

    command("removeInfraction") {
        description = "Use this to delete (permanently) an infraction from a user."
        requiredPermission = Permissions.ADMINISTRATOR
        execute(LowerUserArg, IntegerArg("Infraction ID")) {
            val user = databaseService.users.getOrCreateUser(args.first, guild)
            if (user.getGuildInfo(guild.id.toString()).infractions.isEmpty()) {
                respond("User has no infractions.")
                return@execute
            }
            databaseService.users.removeInfraction(guild, user, args.second)
            respond("Infractions removed.")
        }
    }
}
