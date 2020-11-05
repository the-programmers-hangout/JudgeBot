package me.ddivad.judgebot.embeds

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.entity.Guild
import com.gitlab.kordlib.core.entity.Member
import com.gitlab.kordlib.core.entity.User
import com.gitlab.kordlib.rest.Image
import com.gitlab.kordlib.rest.builder.message.EmbedBuilder
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.dataclasses.GuildMember
import me.ddivad.judgebot.dataclasses.InfractionType
import me.ddivad.judgebot.dataclasses.PunishmentType
import me.ddivad.judgebot.services.DatabaseService
import me.ddivad.judgebot.util.timeBetween
import me.ddivad.judgebot.util.timeToString
import me.jakejmattson.discordkt.api.dsl.CommandEvent
import me.jakejmattson.discordkt.api.dsl.MenuBuilder
import me.jakejmattson.discordkt.api.extensions.addField
import me.jakejmattson.discordkt.api.extensions.addInlineField
import org.joda.time.DateTime
import java.awt.Color
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneOffset
import java.util.*
import java.util.concurrent.TimeUnit
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

suspend fun MenuBuilder.createHistoryEmbed(
        target: User,
        member: GuildMember,
        guild: Guild,
        config: Configuration,
        databaseService: DatabaseService
) {
    val userGuildDetails = member.getGuildInfo(guild.id.value)
    val notes = userGuildDetails.notes
    val infractions = userGuildDetails.infractions
    val paginatedNotes = notes.chunked(4)
    val totalMenuPages = 1 + 1 + if (paginatedNotes.isNotEmpty()) paginatedNotes.size else 1
    val maxPoints = config[guild.id.longValue]?.infractionConfiguration?.pointCeiling
    page {
        color = Color.MAGENTA
        title = "${target.asUser().tag}: Overview"
        thumbnail {
            url = target.asUser().avatar.url
        }
        val memberInGuild = target.asMemberOrNull(guild.id)
        if (memberInGuild != null) {
            addInlineField("Notes", "${notes.size}")
            addInlineField("Infractions", "${infractions.size}")
            addInlineField("Points", "**${member.getPoints(guild)} / $maxPoints**")
            addInlineField("Join date", formatOffsetTime(memberInGuild.joinedAt))
            addInlineField("Creation date", formatOffsetTime(target.id.timeStamp))
            addInlineField("History Invokes", "${userGuildDetails.historyCount}")
        } else {
            addField("**__User not currently in this guild__**", "")
            addInlineField("Notes", "${notes.size}")
            addInlineField("Infractions", "${infractions.size}")
            addInlineField("Points", "**${member.getPoints(guild)} / $maxPoints**")
            addInlineField("Creation date", formatOffsetTime(target.id.timeStamp))
            addInlineField("History Invokes", "${userGuildDetails.historyCount}")
        }

        val currentPunishments = databaseService.guilds.getPunishmentsForUser(guild, target.asUser())
        if (currentPunishments.isNotEmpty()) {
            addField("", "**__Active Punishments__**")
            currentPunishments.forEach {
                addField(
                        "**${it.type}** with **${timeBetween(DateTime(it.clearTime))}** left.",
                        ""
                )
            }
        } else addField("", "")

        if (infractions.size > 0) {
            val lastInfraction = userGuildDetails.infractions[userGuildDetails.infractions.size - 1]
            addField(
                    "**__Most Recent Infraction__**",
                    "Type: **${lastInfraction.type}** :: Weight: **${lastInfraction.points}**\n " +
                            "Issued by **${guild.kord.getUser(Snowflake(lastInfraction.moderator))?.username}** " +
                            "on **${SimpleDateFormat("dd/MM/yyyy").format(Date(lastInfraction.dateTime))}**\n" +
                            "Punishment: **${lastInfraction.punishment?.punishment}** ${
                                if (lastInfraction.punishment?.duration != null) "for **" + timeToString(lastInfraction.punishment?.duration!!) + "**" else "indefinitely"
                            }\n" +
                            lastInfraction.reason
            )
        } else addField("", "**User has no recent infractions.**")
        footer {
            icon = guild.getIconUrl(Image.Format.PNG) ?: ""
            text = "Page 1 of $totalMenuPages"
        }
    }

    page {
        color = Color.MAGENTA
        title = "${target.asUser().tag}: Infractions"
        thumbnail {
            url = target.asUser().avatar.url
        }
        val infractions = userGuildDetails.infractions
        val warnings = infractions.filter { it.type == InfractionType.Warn }
        val strikes = infractions.filter { it.type == InfractionType.Strike }
        val mutes = infractions.filter { it.type == InfractionType.Mute }
        val badpfps = infractions.filter { it.type == InfractionType.BadPfp }
        val bans = infractions.filter { it.punishment?.punishment == PunishmentType.BAN }

        addInlineField("Warns", "${warnings.size}")
        addInlineField("Strikes", "${strikes.size}")
        addInlineField("Bans", "${bans.size}")

        if (warnings.isNotEmpty()) addField("", "**__Warnings__**")
        warnings.forEachIndexed { index, infraction ->
            val moderator = guild.kord.getUser(Snowflake(infraction.moderator))?.username
            addField(
                    "ID :: $index :: Staff :: __${moderator}__",
                    "Type: **${infraction.type} (${infraction.points})** :: " +
                            "Date: **${SimpleDateFormat("dd/MM/yyyy").format(Date(infraction.dateTime))}**\n " +
                            "Punishment: **${infraction.punishment?.punishment}** ${
                                if (infraction.punishment?.duration != null) "for **" + timeToString(infraction.punishment?.duration!!) + "**" else "indefinitely"
                            }\n" +
                            infraction.reason
            )
        }

        if (strikes.isNotEmpty()) addField("", "**__Strikes__**")
        strikes.forEachIndexed { index, infraction ->
            val moderator = guild.kord.getUser(Snowflake(infraction.moderator))?.username
            addField(
                    "ID :: $index :: Staff :: __${moderator}__",
                    "Type: **${infraction.type} (${infraction.points})** :: " +
                            "Date: **${SimpleDateFormat("dd/MM/yyyy").format(Date(infraction.dateTime))}**\n " +
                            "Punishment: **${infraction.punishment?.punishment}** ${
                                if (infraction.punishment?.duration != null) "for **" + timeToString(infraction.punishment?.duration!!) + "**" else "indefinitely"
                            }\n" +
                            infraction.reason
            )
        }

        footer {
            icon = guild.getIconUrl(Image.Format.PNG) ?: ""
            text = "Page 2 of $totalMenuPages"
        }
    }

    if (notes.isEmpty()) {
        page {
            color = Color.MAGENTA
            title = "${target.asUser().tag}: Notes"
            thumbnail {
                url = target.asUser().avatar.url
            }

            addInlineField("Notes", "${notes.size}")
            addInlineField("Infractions", "${infractions.size}")
            addInlineField("Points", "**${member.getPoints(guild)} / $maxPoints**")

            addField("", "**__Notes:__**")
            addField("**No Notes**", "User has no notes written.")
            footer {
                icon = guild.getIconUrl(Image.Format.PNG) ?: ""
                text = "Page 3 of $totalMenuPages"
            }
        }
    }

    paginatedNotes.forEachIndexed { index, list ->
        page {
            color = Color.MAGENTA
            title = "${target.asUser().tag}: Notes" + if (paginatedNotes.size > 1) "(${index + 1})" else ""
            thumbnail {
                url = target.asUser().avatar.url
            }
            list.forEachIndexed { index, note ->
                val moderator = guild.kord.getUser(Snowflake(note.moderator))?.username

                addField(
                        "ID :: ${note.id} :: Staff :: __${moderator}__",
                        "Noted by **${moderator}** on **${SimpleDateFormat("dd/MM/yyyy").format(Date(note.dateTime))}**\n" +
                                note.note
                )
            }
            footer {
                icon = guild.getIconUrl(Image.Format.PNG) ?: ""
                text = "Page ${3 + index} of $totalMenuPages"
            }
        }
    }
}

suspend fun CommandEvent<*>.createStatusEmbed(target: Member,
                                              member: GuildMember,
                                              guild: Guild,
                                              config: Configuration) = respond {
    val userGuildDetails = member.getGuildInfo(guild.id.value)!!
    val notes = userGuildDetails.notes
    val infractions = userGuildDetails.infractions
    val maxPoints = config[guild.id.longValue]?.infractionConfiguration?.pointCeiling

    color = discord.configuration.theme
    title = "${target.asUser().tag}'s Record"
    thumbnail {
        url = target.asUser().avatar.url
    }

    addInlineField("Notes", "${notes.size}")
    addInlineField("Infractions", "${infractions.size}")
    addInlineField("Points", "**${member.getPoints(guild)} / $maxPoints**")
    addInlineField("Join date", formatOffsetTime(target.joinedAt))
    addInlineField("Creation date", formatOffsetTime(target.id.timeStamp))
    addInlineField("History Invokes", "${userGuildDetails.historyCount}")
}

private fun formatOffsetTime(time: Instant): String {
    val days = TimeUnit.MILLISECONDS.toDays(DateTime.now().millis - time.toEpochMilli())
    val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(Locale.UK).withZone(ZoneOffset.UTC);
    return if (days > 4) {
        "${formatter.format(time)}\n($days days ago)"
    } else {
        val hours = TimeUnit.MILLISECONDS.toHours(DateTime.now().millis - time.toEpochMilli())
        "${formatter.format(time)}\n($hours hours ago)"
    }
}

suspend fun EmbedBuilder.createSelfHistoryEmbed(target: User,
                                                member: GuildMember,
                                                guild: Guild,
                                                config: Configuration) {

    val userGuildDetails = member.getGuildInfo(guild.id.value)
    val infractions = userGuildDetails.infractions
    val warnings = userGuildDetails.infractions.filter { it.type == InfractionType.Warn }
    val strikes = userGuildDetails.infractions.filter { it.type == InfractionType.Strike }
    val maxPoints = config[guild.id.longValue]?.infractionConfiguration?.pointCeiling

    color = Color.MAGENTA
    title = "${target.asUser().tag}'s Record"
    thumbnail {
        url = target.asUser().avatar.url
    }
    addInlineField("Infractions", "${infractions.size}")
    addInlineField("Points", "**${member.getPoints(guild)} / $maxPoints**")

    if (infractions.size == 0) {
        addField("", "")
        addField("No infractions issued.", "")
    } else {
        if (warnings.isNotEmpty()) addField("", "**__Warnings__**")
        warnings.forEachIndexed { index, infraction ->
            addField(
                    "ID :: $index :: Weight :: ${infraction.points}",
                    "Type: **${infraction.type} (${infraction.points})** :: " +
                            "Date: **${SimpleDateFormat("dd/MM/yyyy").format(Date(infraction.dateTime))}**\n " +
                            "Punishment: **${infraction.punishment?.punishment}** ${
                                if (infraction.punishment?.duration != null) "for **" + timeToString(infraction.punishment?.duration!!) + "**" else "indefinitely"
                            }\n" +
                            infraction.reason
            )
        }

        if (warnings.isNotEmpty()) addField("", "**__Strikes__**")
        strikes.forEachIndexed { index, infraction ->
            addField(
                    "ID :: $index :: Weight :: ${infraction.points}",
                    "Type: **${infraction.type} (${infraction.points})** :: " +
                            "Date: **${SimpleDateFormat("dd/MM/yyyy").format(Date(infraction.dateTime))}**\n " +
                            "Punishment: **${infraction.punishment?.punishment}** ${
                                if (infraction.punishment?.duration != null) "for **" + timeToString(infraction.punishment?.duration!!) + "**" else "indefinitely"
                            }\n" +
                            infraction.reason
            )
        }
    }

    footer {
        icon = guild.getIconUrl(Image.Format.PNG) ?: ""
        text = guild.name
    }
}
