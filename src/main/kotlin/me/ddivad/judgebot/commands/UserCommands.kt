package me.ddivad.judgebot.commands

import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.embeds.createHistoryEmbed
import me.ddivad.judgebot.embeds.createStatusEmbed
import me.ddivad.judgebot.services.DatabaseService
import me.ddivad.judgebot.services.PermissionLevel
import me.ddivad.judgebot.services.requiredPermissionLevel
import me.jakejmattson.discordkt.api.arguments.MemberArg
import me.jakejmattson.discordkt.api.dsl.commands

fun createUserCommands(databaseService: DatabaseService,
                            config: Configuration) = commands("User") {
    command("history", "h") {
        description = "Use this to view a user's record."
        requiresGuild = true
        requiredPermissionLevel = PermissionLevel.Staff
        execute(MemberArg) {
            val user = databaseService.users.getOrCreateUser(args.first, guild!!.id.value)
            databaseService.users.incrementUserHistory(user, guild!!.id.value)
            createHistoryEmbed(args.first, user, guild!!, config, true)
        }
    }

    command("status", "st") {
        description = "Use this to view a user's status card."
        requiresGuild = true
        requiredPermissionLevel = PermissionLevel.Staff
        execute(MemberArg) {
            val user = databaseService.users.getOrCreateUser(args.first, guild!!.id.value)
            databaseService.users.incrementUserHistory(user, guild!!.id.value)
            createStatusEmbed(args.first, user, guild!!)
        }
    }
}