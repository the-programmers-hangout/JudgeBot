package me.ddivad.judgebot.commands

import com.gitlab.kordlib.core.behavior.ban
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.embeds.createHistoryEmbed
import me.ddivad.judgebot.embeds.createStatusEmbed
import me.ddivad.judgebot.services.DatabaseService
import me.ddivad.judgebot.services.LoggingService
import me.ddivad.judgebot.services.PermissionLevel
import me.ddivad.judgebot.services.requiredPermissionLevel
import me.jakejmattson.discordkt.api.arguments.EveryArg
import me.jakejmattson.discordkt.api.arguments.IntegerArg
import me.jakejmattson.discordkt.api.arguments.MemberArg
import me.jakejmattson.discordkt.api.arguments.UserArg
import me.jakejmattson.discordkt.api.dsl.commands
import java.awt.Color

fun createUserCommands(databaseService: DatabaseService,
                       config: Configuration,
                       loggingService: LoggingService) = commands("User") {
    guildCommand("history", "h") {
        description = "Use this to view a user's record."
        requiredPermissionLevel = PermissionLevel.Staff
        execute(MemberArg) {
            val user = databaseService.users.getOrCreateUser(args.first, guild.id.value)
            databaseService.users.incrementUserHistory(user, guild.id.value)
            respondMenu {
                createHistoryEmbed(args.first, user, guild, config, true)
            }
        }
    }

    guildCommand("status", "st") {
        description = "Use this to view a user's status card."
        requiredPermissionLevel = PermissionLevel.Staff
        execute(MemberArg) {
            val user = databaseService.users.getOrCreateUser(args.first, guild.id.value)
            databaseService.users.incrementUserHistory(user, guild.id.value)
            createStatusEmbed(args.first, user, guild)
        }
    }

    guildCommand("whatpfp") {
        description = "Perform a reverse image search of a User's profile picture"
        requiredPermissionLevel = PermissionLevel.Staff
        execute(UserArg) {
            val user = args.first
            val reverseSearchUrl = "<https://www.google.com/searchbyimage?&image_url=${user.avatar.url}>"
            respond {
                title = "${user.tag}'s pfp"
                color = Color.MAGENTA
                description = "[Reverse Search]($reverseSearchUrl)"
                image = "${user.avatar.url}?size=512"
            }
        }
    }

    guildCommand("ban") {
        description = "Ban a member from this guild."
        requiredPermissionLevel = PermissionLevel.Staff
        execute(MemberArg, IntegerArg("Delete message days").makeOptional(1), EveryArg) {
            val (target, deleteDays, reason) = args
            guild.ban(target.id) {
                this.reason = reason
                this.deleteMessagesDays = deleteDays
                databaseService.guilds.banUser(guild, target.id.value, author.id.value, reason).also {
                    loggingService.userBanned(guild, target.asUser(), it)
                    respond("User ${target.tag} banned")
                }
            }
        }
    }

    guildCommand("unban") {
        description = "Unban a banned member from this guild."
        requiredPermissionLevel = PermissionLevel.Staff
        execute(UserArg) {
            val user = args.first
            guild.unban(user.id)
            databaseService.guilds.removeBan(guild, user.id.value).also {
                loggingService.userUnbanned(guild, user)
                respond("${user.tag} unbanned")
            }
        }
    }
}