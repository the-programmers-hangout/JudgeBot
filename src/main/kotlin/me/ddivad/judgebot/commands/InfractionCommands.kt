package me.ddivad.judgebot.commands

import com.gitlab.kordlib.common.exception.RequestException
import me.ddivad.judgebot.arguments.LowerMemberArg
import me.ddivad.judgebot.conversations.StrikeConversation
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.dataclasses.Infraction
import me.ddivad.judgebot.dataclasses.InfractionType
import me.ddivad.judgebot.embeds.createHistoryEmbed
import me.ddivad.judgebot.extensions.testDmStatus
import me.ddivad.judgebot.services.*
import me.ddivad.judgebot.services.infractions.BadPfpService
import me.ddivad.judgebot.services.infractions.InfractionService
import me.jakejmattson.discordkt.api.arguments.BooleanArg
import me.jakejmattson.discordkt.api.arguments.EveryArg
import me.jakejmattson.discordkt.api.arguments.IntegerArg
import me.jakejmattson.discordkt.api.dsl.commands

fun createInfractonCommands(databaseService: DatabaseService,
                            config: Configuration,
                            infractionService: InfractionService,
                            badPfpService: BadPfpService) = commands("Infraction") {
    guildCommand("strike", "s") {
        description = "Strike a user."
        requiredPermissionLevel = PermissionLevel.Staff
        execute(LowerMemberArg, IntegerArg.makeOptional(1), EveryArg) {
            val (targetMember, weight, reason) = args
            try {
                targetMember.testDmStatus()
            } catch (ex: RequestException) {
                respond("Unable to contact the target user. Infraction cancelled.")
                return@execute
            }
            StrikeConversation(databaseService, config, infractionService)
                    .createStrikeConversation(guild, targetMember, weight, reason)
                    .startPublicly(discord, author, channel)
        }
    }

    guildCommand("warn", "w") {
        description = "Warn a user."
        requiredPermissionLevel = PermissionLevel.Moderator
        execute(LowerMemberArg, EveryArg) {
            val (targetMember, reason) = args
            try {
                targetMember.testDmStatus()
            } catch (ex: RequestException) {
                respond("Unable to contact the target user. Infraction cancelled.")
                return@execute
            }
            val guildConfiguration = config[guild.id.longValue] ?: return@execute
            val user = databaseService.users.getOrCreateUser(targetMember, guild)
            val infraction = Infraction(this.author.id.value, reason, InfractionType.Warn, guildConfiguration.infractionConfiguration.warnPoints)
            infractionService.infract(targetMember, guild, user, infraction)
            respondMenu {
                createHistoryEmbed(targetMember, user, guild, config, databaseService)
            }
        }
    }

    guildCommand("badpfp") {
        description = "Notifies the user that they should change their profile pic and applies a 30 minute mute. Bans the user if they don't change picture."
        requiredPermissionLevel = PermissionLevel.Staff
        execute(BooleanArg("cancel", "apply", "cancel").makeOptional(true), LowerMemberArg) {
            val (cancel, targetMember) = args
            try {
                targetMember.testDmStatus()
            } catch (ex: RequestException) {
                respond("Unable to contact the target user. Infraction cancelled.")
                return@execute
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

            val badPfp = Infraction(author.id.value, "BadPfp", InfractionType.BadPfp)
            badPfpService.applyBadPfp(targetMember, guild, badPfp, timeLimit)
            respond("${targetMember.mention} has been muted and a badpfp has been triggered with a time limit of $minutesUntilBan minutes.")
        }
    }

    guildCommand("cleanse") {
        description = "Use this to delete (permanently) as user's infractions."
        requiredPermissionLevel = PermissionLevel.Administrator
        execute(LowerMemberArg) {
            val user = databaseService.users.getOrCreateUser(args.first, guild)
            if (user.getGuildInfo(guild.id.value).infractions.isEmpty()) {
                respond("User has no infractions.")
                return@execute
            }
            databaseService.users.cleanseInfractions(guild, user)
            respond("Infractions cleansed.")
        }
    }

    guildCommand("removeInfraction") {
        description = "Use this to delete (permanently) an infraction from a user."
        requiredPermissionLevel = PermissionLevel.Administrator
        execute(LowerMemberArg, IntegerArg("Infraction ID")) {
            val user = databaseService.users.getOrCreateUser(args.first, guild)
            if (user.getGuildInfo(guild.id.value).infractions.isEmpty()) {
                respond("User has no infractions.")
                return@execute
            }
            databaseService.users.removeInfraction(guild, user, args.second)
            respond("Infractions removed.")
        }
    }
}