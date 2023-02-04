package me.ddivad.judgebot.commands

import dev.kord.common.entity.ApplicationCommandType
import dev.kord.common.kColor
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.rest.Image
import dev.kord.x.emoji.Emojis
import me.ddivad.judgebot.arguments.LowerUserArg
import me.ddivad.judgebot.dataclasses.Ban
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.dataclasses.Permissions
import me.ddivad.judgebot.embeds.createHistoryEmbed
import me.ddivad.judgebot.embeds.createLinkedAccountMenu
import me.ddivad.judgebot.services.DatabaseService
import me.ddivad.judgebot.services.LoggingService
import me.ddivad.judgebot.services.infractions.BanService
import me.jakejmattson.discordkt.arguments.*
import me.jakejmattson.discordkt.commands.commands
import me.jakejmattson.discordkt.commands.subcommand
import me.jakejmattson.discordkt.extensions.createMenu
import me.jakejmattson.discordkt.extensions.pfpUrl
import java.awt.Color
import java.text.SimpleDateFormat

@Suppress("unused")
fun createUserCommands(
    databaseService: DatabaseService,
    config: Configuration,
    loggingService: LoggingService,
    banService: BanService
) = commands("User") {
    slash("history", "Use this to view a user's record.", Permissions.MODERATOR) {
        execute(UserArg) {
            val interactionResponse = interaction?.deferPublicResponse() ?: return@execute
            val user = databaseService.users.getOrCreateUser(args.first, guild)
            databaseService.users.incrementUserHistory(user, guild)
            channel.createMenu { createHistoryEmbed(args.first, user, guild, config, databaseService) }
            interactionResponse.respond { content = "History for ${args.first.mention}:" }
        }
    }

    slash("ban", "Ban a member from this guild.", Permissions.STAFF) {
        execute(LowerUserArg, EveryArg("Reason"), IntegerArg("Days", "Delete message days").optional(0)) {
            val (target, reason, deleteDays) = args
            if (deleteDays > 7) {
                respond("Delete days cannot be more than **7**. You tried with **${deleteDays}**")
                return@execute
            }
            val ban = Ban(target.id.toString(), author.id.toString(), reason)
            banService.banUser(target, guild, ban, deleteDays).also {
                loggingService.userBanned(guild, target, ban)
                respondPublic("User ${target.mention} banned\n**Reason**: $reason")
            }
        }
    }

    slash("unban", "Unban a banned member from this guild.", Permissions.STAFF) {
        execute(
            UserArg,
            BooleanArg(
                "Thin-Ice",
                "true",
                "false",
                "Unban user in 'Thin Ice' mode which sets their points to 40 and freezes their point decay"
            ).optional(false)
        ) {
            val (user, thinIce) = args
            guild.getBanOrNull(user.id)?.let {
                banService.unbanUser(user, guild, thinIce)
                respondPublic("${user.tag} unbanned ${if (thinIce) "in thin ice mode" else ""}")
                return@execute
            }
            respondPublic("${user.mention} isn't banned from this guild.")
        }
    }

    slash("alt", "Link, Unlink or view a user's alt accounts", Permissions.STAFF) {
        execute(
            ChoiceArg("Option", "Alt options", "Link", "Unlink", "View"),
            UserArg("Main"),
            UserArg("Alt").optionalNullable(null)
        ) {
            val (option, user, alt) = args
            val mainRecord = databaseService.users.getOrCreateUser(user, guild)
            when (option) {
                "Link" -> {
                    if (alt == null) {
                        respond("Missing alt account argument")
                        return@execute
                    }
                    val altRecord = databaseService.users.getOrCreateUser(alt, guild)
                    databaseService.users.addLinkedAccount(guild, mainRecord, alt.id.toString())
                    databaseService.users.addLinkedAccount(guild, altRecord, user.id.toString())
                    respondPublic("Linked accounts ${user.mention} and ${alt.mention}")
                }
                "Unlink" -> {
                    if (alt == null) {
                        respond("Missing alt account argument")
                        return@execute
                    }
                    val altRecord = databaseService.users.getOrCreateUser(alt, guild)
                    databaseService.users.removeLinkedAccount(guild, mainRecord, alt.id.toString())
                    databaseService.users.removeLinkedAccount(guild, altRecord, user.id.toString())
                    respondPublic("Unlinked accounts ${user.mention} and ${alt.mention}")
                }
                "View" -> {
                    databaseService.users.incrementUserHistory(mainRecord, guild)
                    val linkedAccounts = mainRecord.getLinkedAccounts(guild)

                    if (linkedAccounts.isEmpty()) {
                        respond("User ${user.mention} has no alt accounts recorded.")
                        return@execute
                    }
                    respondPublic("Alt accounts for ${user.mention}")
                    channel.createMenu { createLinkedAccountMenu(linkedAccounts, guild, config, databaseService) }
                }
            }
        }
    }

    user("PFP Lookup", "whatpfp","Perform a reverse image search of a User's profile picture", Permissions.EVERYONE) {
        val user = args.first
        val reverseSearchUrl = "<https://lens.google.com/uploadbyurl?url=${user.pfpUrl}>"

        if (interaction?.invokedCommandType == ApplicationCommandType.User) {
            respond {
                title = "${user.tag}'s pfp"
                color = Color.MAGENTA.kColor
                description = "[Reverse Search]($reverseSearchUrl)"
                image = "${user.pfpUrl}?size=512"
            }
        } else {
            respondPublic {
                title = "${user.tag}'s pfp"
                color = Color.MAGENTA.kColor
                description = "[Reverse Search]($reverseSearchUrl)"
                image = "${user.pfpUrl}?size=512"
            }
        }

    }

    slash("deletedMessages", "View a users messages deleted using the delete message reaction", Permissions.STAFF) {
        execute(UserArg) {
            val target = args.first
            val guildMember = databaseService.users.getOrCreateUser(target, guild).getGuildInfo(guild.id.toString())
            val guildConfiguration = config[guild.asGuild().id]

            val deletedMessages = databaseService.messageDeletes
                .getMessageDeletesForMember(guild.id.toString(), target.id.toString())
                .sortedByDescending { it.dateTime }
                .map { "Deleted on **${SimpleDateFormat("dd/MM/yyyy HH:mm").format(it.dateTime)}** \n[Message Link](${it.messageLink})" }
                .chunked(6)

            if (deletedMessages.isEmpty()) {
                respond("User has no messages deleted using ${guildConfiguration?.reactions?.deleteMessageReaction}")
                return@execute
            }

            respondPublic("Deleted messages for ${target.mention}")
            channel.createMenu {
                deletedMessages.forEachIndexed { index, list ->
                    page {
                        color = discord.configuration.theme
                        author {
                            name = "Deleted messages for ${target.tag}"
                            icon = target.pfpUrl
                        }
                        description = """
                            **Showing messages deleted using ${guildConfiguration?.reactions?.deleteMessageReaction}**
                            ${target.tag} has **${guildMember.deletedMessageCount.deleteReaction}** deletions
                        """.trimIndent()

                        list.forEach {
                            field {
                                value = it
                            }
                        }

                        footer {
                            icon = guild.getIconUrl(Image.Format.PNG) ?: ""
                            text = "${guild.name} | Page ${index + 1} of ${deletedMessages.size}"
                        }
                    }
                }
                if (deletedMessages.size > 1) {
                    buttons {
                        button("Prev.", Emojis.arrowLeft) {
                            previousPage()
                        }
                        button("Next", Emojis.arrowRight) {
                            nextPage()
                        }
                    }
                }
            }
        }
    }
}

@Suppress("Unused")
fun banReasonSubCommands(databaseService: DatabaseService) = subcommand("BanReason", Permissions.STAFF) {
    sub("get", "Get the ban reason for a user if it exists.") {
        execute(UserArg) {
            val user = args.first
            guild.getBanOrNull(user.id)?.let {
                val reason = databaseService.guilds.getBanOrNull(guild, user.id.toString())?.reason ?: it.reason
                respondPublic(reason ?: "No reason logged")
                return@execute
            }
            respond("${user.username} isn't banned from this guild.")
        }
    }

    sub("set", "Set a ban reason for a user.") {
        execute(UserArg, EveryArg("Reason")) {
            val (user, reason) = args
            val ban = Ban(user.id.toString(), author.id.toString(), reason)
            if (guild.getBanOrNull(user.id) != null) {
                if (!databaseService.guilds.checkBanExists(guild, user.id.toString())) {
                    databaseService.guilds.addBan(guild, ban)
                } else {
                    databaseService.guilds.editBanReason(guild, user.id.toString(), reason)
                }
                respondPublic("Ban reason for ${user.username} set to: $reason")
            } else respond("User ${user.username} isn't banned")
        }
    }
}