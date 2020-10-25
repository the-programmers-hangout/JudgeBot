package me.ddivad.judgebot.commands

import me.ddivad.judgebot.conversations.InfractionConversation
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.dataclasses.Infraction
import me.ddivad.judgebot.dataclasses.InfractionType
import me.ddivad.judgebot.embeds.createHistoryEmbed
import me.ddivad.judgebot.services.*
import me.ddivad.judgebot.services.infractions.InfractionService
import me.jakejmattson.discordkt.api.arguments.EveryArg
import me.jakejmattson.discordkt.api.arguments.IntegerArg
import me.jakejmattson.discordkt.api.arguments.MemberArg
import me.jakejmattson.discordkt.api.dsl.commands

fun createInfractonCommands(databaseService: DatabaseService,
                            config: Configuration,
                            infractionService: InfractionService) = commands("Infraction") {
    guildCommand("strike", "s") {
        description = "Strike a user."
        requiredPermissionLevel = PermissionLevel.Staff
        execute(MemberArg, IntegerArg.makeOptional(1), EveryArg) {
            val (user, weight, reason) = args
            InfractionConversation(databaseService, config, infractionService)
                    .createInfractionConversation(guild, user, weight, reason)
                    .startPublicly(discord, author, channel)
        }
    }

    guildCommand("warn", "w") {
        description = "Warn a user."
        requiredPermissionLevel = PermissionLevel.Staff
        execute(MemberArg, EveryArg) {
            val guildConfiguration = config[guild.id.longValue] ?: return@execute
            val user = databaseService.users.getOrCreateUser(args.first, guild.id.value)
            val infraction = Infraction(this.author.id.value, args.second, InfractionType.Warn, guildConfiguration.infractionConfiguration.warnPoints)
            infractionService.infract(args.first, guild, user, infraction)
            respondMenu {
                createHistoryEmbed(args.first, user, guild, config, true)
            }
        }
    }

    guildCommand("cleanse") {
        description = "Use this to delete (permanently) as user's infractions."
        requiredPermissionLevel = PermissionLevel.Administrator
        execute(MemberArg) {
            val user = databaseService.users.getOrCreateUser(args.first, guild.id.value)
            if (user.getGuildInfo(guild.id.value)!!.infractions.isEmpty()) {
                respond("User has no infractions.")
                return@execute
            }
            databaseService.users.cleanseInfractions(guild, user)
            respond("Infractions cleansed.")
        }
    }
}