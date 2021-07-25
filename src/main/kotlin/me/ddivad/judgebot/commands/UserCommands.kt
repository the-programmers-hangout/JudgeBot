package me.ddivad.judgebot.commands

import dev.kord.common.kColor
import dev.kord.x.emoji.Emojis
import dev.kord.x.emoji.addReaction
import kotlinx.coroutines.flow.toList
import me.ddivad.judgebot.arguments.LowerUserArg
import me.ddivad.judgebot.conversations.ResetUserConversation
import me.ddivad.judgebot.conversations.guildChoiceConversation
import me.ddivad.judgebot.dataclasses.*
import me.ddivad.judgebot.embeds.createHistoryEmbed
import me.ddivad.judgebot.embeds.createLinkedAccountMenu
import me.ddivad.judgebot.embeds.createSelfHistoryEmbed
import me.ddivad.judgebot.services.DatabaseService
import me.ddivad.judgebot.services.LoggingService
import me.ddivad.judgebot.services.infractions.BanService
import me.jakejmattson.discordkt.api.arguments.*
import me.jakejmattson.discordkt.api.dsl.commands
import me.jakejmattson.discordkt.api.extensions.mutualGuilds
import me.jakejmattson.discordkt.api.extensions.sendPrivateMessage
import java.awt.Color

@Suppress("unused")
fun createUserCommands(
    databaseService: DatabaseService,
    config: Configuration,
    loggingService: LoggingService,
    banService: BanService
) = commands("User") {
    guildCommand("history", "h", "H") {
        description = "Use this to view a user's record."
        requiredPermission = Permissions.MODERATOR
        execute(UserArg) {
            val user = databaseService.users.getOrCreateUser(args.first, guild)
            databaseService.users.incrementUserHistory(user, guild)
            val linkedAccounts = user.getLinkedAccounts(guild)
            respondMenu {
                createHistoryEmbed(args.first, user, guild, config, databaseService)
            }
        }
    }

    guildCommand("alts") {
        description = "Use this to view a user's alt accounts."
        requiredPermission = Permissions.MODERATOR
        execute(UserArg) {
            val target = args.first
            val user = databaseService.users.getOrCreateUser(args.first, guild)
            databaseService.users.incrementUserHistory(user, guild)
            val linkedAccounts = user.getLinkedAccounts(guild)

            if (linkedAccounts.isEmpty()) {
                respond("User ${target.mention} has no alt accounts recorded.")
                return@execute
            }

            respondMenu {
                createLinkedAccountMenu(linkedAccounts, guild, config, databaseService)
            }
        }
    }

    guildCommand("whatpfp") {
        description = "Perform a reverse image search of a User's profile picture"
        requiredPermission = Permissions.MODERATOR
        execute(UserArg) {
            val user = args.first
            val reverseSearchUrl = "<https://www.google.com/searchbyimage?&image_url=${user.avatar.url}>"
            respond {
                title = "${user.tag}'s pfp"
                color = Color.MAGENTA.kColor
                description = "[Reverse Search]($reverseSearchUrl)"
                image = "${user.avatar.url}?size=512"
            }
        }
    }

    guildCommand("ban") {
        description = "Ban a member from this guild."
        requiredPermission = Permissions.STAFF
        execute(LowerUserArg, IntegerArg("Delete message days").optional(0), EveryArg) {
            val (target, deleteDays, reason) = args
            val ban = Punishment(target.id.asString, InfractionType.Ban, reason, author.id.asString)
            banService.banUser(target, guild, ban, deleteDays).also {
                loggingService.userBanned(guild, target, ban)
                respond("User ${target.mention} banned")
            }
        }
    }

    guildCommand("unban") {
        description = "Unban a banned member from this guild."
        requiredPermission = Permissions.STAFF
        execute(UserArg) {
            val user = args.first
            guild.getBanOrNull(user.id)?.let {
                banService.unbanUser(guild, user)
                respond("${user.tag} unbanned")
                return@execute
            }
            respond("${user.mention} isn't banned from this guild.")
        }
    }

    guildCommand("setBanReason") {
        description = "Set a ban reason for a banned user"
        requiredPermission = Permissions.STAFF
        execute(UserArg, EveryArg("Reason")) {
            val (user, reason) = args
            val ban = Ban(user.id.asString, author.id.asString, reason)
            if (guild.getBanOrNull(user.id) != null) {
                if (!databaseService.guilds.checkBanExists(guild, user.id.asString)) {
                    databaseService.guilds.addBan(guild, ban)
                } else {
                    databaseService.guilds.editBanReason(guild, user.id.asString, reason)
                }
                respond("Ban reason for ${user.username} set to: $reason")
            } else respond("User ${user.username} isn't banned")

        }
    }

    guildCommand("getBanReason") {
        description = "Get a ban reason for a banned user"
        requiredPermission = Permissions.STAFF
        execute(UserArg) {
            val user = args.first
            guild.getBanOrNull(user.id)?.let {
                val reason = databaseService.guilds.getBanOrNull(guild, user.id.asString)?.reason ?: it.reason
                respond(reason ?: "No reason logged")
                return@execute
            }
            respond("${user.username} isn't banned from this guild.")
        }
    }

    command("selfHistory") {
        description = "View your infraction history (contents will be DM'd)"
        requiredPermission = Permissions.NONE
        execute {
            val user = author
            val mutualGuilds = author.mutualGuilds.toList().filter { config[it.id.value] != null }

            if (mutualGuilds.size == 1 || guild != null) {
                val currentGuild = guild ?: mutualGuilds.first()
                val guildMember = databaseService.users.getOrCreateUser(user, currentGuild)

                user.sendPrivateMessage {
                    createSelfHistoryEmbed(user, guildMember, currentGuild, config)
                }
                this.message.addReaction(Emojis.whiteCheckMark)
            } else {
                guildChoiceConversation(mutualGuilds, config).startPrivately(discord, author)
            }
        }
    }

    guildCommand("link") {
        description = "Link a user's alt account with their main"
        requiredPermission = Permissions.STAFF
        execute(UserArg("Main Account"), UserArg("Alt Account")) {
            val (main, alt) = args
            val mainRecord = databaseService.users.getOrCreateUser(main, guild)
            val altRecord = databaseService.users.getOrCreateUser(alt, guild)
            databaseService.users.addLinkedAccount(guild, mainRecord, alt.id.asString)
            databaseService.users.addLinkedAccount(guild, altRecord, main.id.asString)
            respond("Linked accounts ${main.mention} and ${alt.mention}")
        }
    }

    guildCommand("unlink") {
        description = "Link a user's alt account with their main"
        requiredPermission = Permissions.STAFF
        execute(UserArg("Main Account"), UserArg("Alt Account")) {
            val (main, alt) = args
            val mainRecord = databaseService.users.getOrCreateUser(main, guild)
            val altRecord = databaseService.users.getOrCreateUser(alt, guild)
            databaseService.users.removeLinkedAccount(guild, mainRecord, alt.id.asString)
            databaseService.users.removeLinkedAccount(guild, altRecord, main.id.asString)
            respond("Unlinked accounts ${main.mention} and ${alt.mention}")
        }
    }

    guildCommand("reset") {
        description = "Reset a user's record, and any linked accounts"
        requiredPermission = Permissions.STAFF
        execute(LowerUserArg) {
            val target = args.first
            ResetUserConversation(databaseService, config)
                .createResetConversation(guild, target)
                .startPublicly(discord, author, channel)
        }
    }
}
