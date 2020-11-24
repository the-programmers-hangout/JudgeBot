package me.ddivad.judgebot.embeds

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.entity.Guild
import com.gitlab.kordlib.core.entity.User
import com.gitlab.kordlib.rest.Image
import com.gitlab.kordlib.rest.builder.message.EmbedBuilder
import me.ddivad.judgebot.dataclasses.*
import me.ddivad.judgebot.services.DatabaseService
import me.ddivad.judgebot.util.*
import me.jakejmattson.discordkt.api.dsl.MenuBuilder
import me.jakejmattson.discordkt.api.extensions.addField
import me.jakejmattson.discordkt.api.extensions.addInlineField
import org.joda.time.DateTime
import java.awt.Color
import java.text.SimpleDateFormat
import java.util.*

suspend fun MenuBuilder.createHistoryEmbed(
        target: User,
        member: GuildMember,
        guild: Guild,
        config: Configuration,
        databaseService: DatabaseService
) {
    val userRecord = member.getGuildInfo(guild.id.value)
    val paginatedNotes = userRecord.notes.chunked(4)
    val totalMenuPages = 1 + 1 + 1 + 1 + if (paginatedNotes.isNotEmpty()) paginatedNotes.size else 1
    val guildConfiguration = config[guild.id.longValue]!!
    val embedColor = getEmbedColour(guild, target, databaseService)
    this.apply {
        buildOverviewPage(guild, guildConfiguration, target, userRecord, embedColor, totalMenuPages, databaseService)
        buildInfractionPage(guild, target, userRecord, embedColor, totalMenuPages)
        buildNotesPages(guild, guildConfiguration, target, userRecord, embedColor, totalMenuPages)
        buildInformationPage(guild, guildConfiguration, target, userRecord, embedColor, totalMenuPages)
        buildJoinLeavePage(guild, target, userRecord, embedColor, totalMenuPages)
    }
}

private suspend fun MenuBuilder.buildOverviewPage(
        guild: Guild,
        config: GuildConfiguration,
        target: User,
        userRecord: GuildMemberDetails,
        embedColor: Color,
        totalPages: Int,
        databaseService: DatabaseService) {
    page {
        color = embedColor
        title = "${target.asUser().tag}: Overview"
        thumbnail {
            url = target.asUser().avatar.url
        }
        val memberInGuild = target.asMemberOrNull(guild.id)
        addInlineField("Notes", "${userRecord.notes.size}")
        addInlineField("Infractions", "${userRecord.infractions.size}")
        addInlineField("Points", "**${userRecord.points} / ${config.infractionConfiguration.pointCeiling}**")
        addInlineField("History Invokes", "${userRecord.historyCount}")
        addInlineField("Creation date", formatOffsetTime(target.id.timeStamp))
        if (memberInGuild != null) {
            addInlineField("Join date", formatOffsetTime(memberInGuild.joinedAt))
        } else addInlineField("", "")

        getStatus(guild, target, databaseService)?.let { addField("Status", it) }

        if (userRecord.infractions.size > 0) {
            val lastInfraction = userRecord.infractions[userRecord.infractions.size - 1]
            addField(
                    "**__Most Recent Infraction__**",
                    "Type: **${lastInfraction.type}** :: Weight: **${lastInfraction.points}**\n " +
                            "Issued by **${guild.kord.getUser(Snowflake(lastInfraction.moderator))?.username}** " +
                            "on **${SimpleDateFormat("dd/MM/yyyy").format(Date(lastInfraction.dateTime))}**\n" +
                            "Punishment: **${lastInfraction.punishment?.punishment}** ${getDurationText(lastInfraction.punishment)}\n" +
                            lastInfraction.reason
            )
        } else addField("", "**User has no recent infractions**")
        footer {
            icon = guild.getIconUrl(Image.Format.PNG) ?: ""
            text = "Page 1 of $totalPages"
        }
    }
}

private suspend fun MenuBuilder.buildInfractionPage(
        guild: Guild,
        target: User,
        userRecord: GuildMemberDetails,
        embedColor: Color,
        totalPages: Int
) {
    page {
        color = embedColor
        title = "${target.asUser().tag}: Infractions"
        thumbnail {
            url = target.asUser().avatar.url
        }
        val warnings = userRecord.infractions.filter { it.type == InfractionType.Warn }
        val strikes = userRecord.infractions.filter { it.type == InfractionType.Strike }
        val bans = userRecord.infractions.filter { it.punishment?.punishment == PunishmentType.BAN }

        addInlineField("Warns", "${warnings.size}")
        addInlineField("Strikes", "${strikes.size}")
        addInlineField("Bans", "${bans.size}")

        if (userRecord.infractions.isEmpty()) addField("", "**User has no infractions**")
        if (warnings.isNotEmpty()) addField("", "**__Warnings__**")
        warnings.forEachIndexed { _, infraction ->
            val moderator = guild.kord.getUser(Snowflake(infraction.moderator))?.username
            addField(
                    "ID :: ${infraction.id} :: Staff :: __${moderator}__",
                    "Type: **${infraction.type} (${infraction.points})** :: " +
                            "Date: **${SimpleDateFormat("dd/MM/yyyy").format(Date(infraction.dateTime))}**\n " +
                            "Punishment: **${infraction.punishment?.punishment}** ${getDurationText(infraction.punishment)}\n" +
                            infraction.reason
            )
        }

        if (strikes.isNotEmpty()) addField("", "**__Strikes__**")
        strikes.forEachIndexed { _, infraction ->
            val moderator = guild.kord.getUser(Snowflake(infraction.moderator))?.username
            addField(
                    "ID :: ${infraction.id} :: Staff :: __${moderator}__",
                    "Type: **${infraction.type} (${infraction.points})** :: " +
                            "Date: **${SimpleDateFormat("dd/MM/yyyy").format(Date(infraction.dateTime))}**\n " +
                            "Punishment: **${infraction.punishment?.punishment}** ${getDurationText(infraction.punishment)}\n" +
                            infraction.reason
            )
        }

        footer {
            icon = guild.getIconUrl(Image.Format.PNG) ?: ""
            text = "Page 2 of $totalPages"
        }
    }
}

private suspend fun MenuBuilder.buildNotesPages(
        guild: Guild,
        config: GuildConfiguration,
        target: User,
        userRecord: GuildMemberDetails,
        embedColor: Color,
        totalPages: Int
) {
    val paginatedNotes = userRecord.notes.chunked(4)
    if (userRecord.notes.isEmpty()) {
        page {
            color = embedColor
            title = "${target.asUser().tag}: Notes"
            thumbnail {
                url = target.asUser().avatar.url
            }

            addInlineField("Notes", "${userRecord.notes.size}")
            addInlineField("Information", "${userRecord.info.size}")
            addInlineField("Points", "**${userRecord.points} / ${config.infractionConfiguration.pointCeiling}**")

            addField("", "**User has no notes**")
            footer {
                icon = guild.getIconUrl(Image.Format.PNG) ?: ""
                text = "Page 3 of $totalPages"
            }
        }
    }

    paginatedNotes.forEachIndexed { index, list ->
        page {
            color = embedColor
            title = "${target.asUser().tag}: Notes" + if (paginatedNotes.size > 1) "(${index + 1})" else ""
            thumbnail {
                url = target.asUser().avatar.url
            }
            addInlineField("Notes", "${userRecord.notes.size}")
            addInlineField("Information", "${userRecord.info.size}")
            addInlineField("Points", "**${userRecord.points} / ${config.infractionConfiguration.pointCeiling}**")

            list.forEachIndexed { _, note ->
                val moderator = guild.kord.getUser(Snowflake(note.moderator))?.username

                addField(
                        "ID :: ${note.id} :: Staff :: __${moderator}__",
                        "Noted by **${moderator}** on **${SimpleDateFormat("dd/MM/yyyy").format(Date(note.dateTime))}**\n" +
                                note.note
                )
            }
            footer {
                icon = guild.getIconUrl(Image.Format.PNG) ?: ""
                text = "Page ${3 + index} of $totalPages"
            }
        }
    }
}

private suspend fun MenuBuilder.buildInformationPage(
        guild: Guild,
        config: GuildConfiguration,
        target: User,
        userRecord: GuildMemberDetails,
        embedColor: Color,
        totalPages: Int
) {
    val paginatedNotes = userRecord.notes.chunked(4)
    page {
        color = embedColor
        title = "${target.asUser().tag}: Information"
        thumbnail {
            url = target.asUser().avatar.url
        }
        addInlineField("Notes", "${userRecord.notes.size}")
        addInlineField("Information", "${userRecord.info.size}")
        addInlineField("Points", "**${userRecord.points} / ${config.infractionConfiguration.pointCeiling}**")

        if (userRecord.info.isEmpty()) addField("", "**User has no information**")
        userRecord.info.forEachIndexed { _, info ->
            val moderator = guild.kord.getUser(Snowflake(info.moderator))?.username
            addField(
                    "ID :: ${info.id} :: Staff :: __${moderator}__",
                    "Sent by **${moderator}** on **${SimpleDateFormat("dd/MM/yyyy").format(Date(info.dateTime))}**\n" +
                            info.message
            )
        }
        footer {
            icon = guild.getIconUrl(Image.Format.PNG) ?: ""
            text = "Page ${3 + if (paginatedNotes.isEmpty()) 1 else paginatedNotes.size} of $totalPages"
        }
    }
}

private suspend fun MenuBuilder.buildJoinLeavePage(
        guild: Guild,
        target: User,
        userRecord: GuildMemberDetails,
        embedColor: Color,
        totalPages: Int
) {
    page {
        val history = userRecord.leaveHistory
        val leaves = history.filter { it.leaveDate != null }
        val paginatedNotes = userRecord.notes.chunked(4)

        color = embedColor
        title = "${target.asUser().tag}: Join / Leave"
        thumbnail {
            url = target.asUser().avatar.url
        }

        addInlineField("Joins:", history.size.toString())
        addInlineField("", "")
        addInlineField("Leaves:", leaves.size.toString())
        addField("", "")
        userRecord.leaveHistory.forEachIndexed { index, record ->
            addInlineField("Record", "#${index + 1}")
            addInlineField("Joined", SimpleDateFormat("dd/MM/yyyy").format(Date(record.joinDate!!)))
            addInlineField("Left", if (record.leaveDate == null) "-" else SimpleDateFormat("dd/MM/yyyy").format(Date(record.joinDate)))

        }
        footer {
            icon = guild.getIconUrl(Image.Format.PNG) ?: ""
            text = "Page ${4 + if (paginatedNotes.isEmpty()) 1 else paginatedNotes.size} of $totalPages"
        }
    }
}

private fun getDurationText(level: PunishmentLevel?): String {
    if (level == null) return ""
    return when {
        level.punishment == PunishmentType.NONE -> {
            ""
        }
        level.duration != null -> {
            "for **" + timeToString(level.duration!!) + "**"
        }
        else -> {
            "indefinitely"
        }
    }
}

private suspend fun getEmbedColour(guild: Guild, target: User, databaseService: DatabaseService): Color {
    if (guild.getBanOrNull(target.id) != null) return Color.RED
    if (target.asMemberOrNull(guild.id) == null) return Color.BLACK
    if (databaseService.guilds.getPunishmentsForUser(guild, target).isNotEmpty()) return Color.ORANGE
    return Color.MAGENTA
}

private suspend fun getStatus(guild: Guild, target: User, databaseService: DatabaseService): String? {
    guild.getBanOrNull(target.id)?.let {
        return "```css\nUser is banned with reason:\n${it.reason}```"
    }
    if (target.asMemberOrNull(guild.id) == null) return "```css\nUser not currently in this guild```"
    databaseService.guilds.getPunishmentsForUser(guild, target).firstOrNull()?.let {
        return "```css\nActive ${it.type} with ${timeBetween(DateTime(it.clearTime))} left```"
    }
    return null
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

    if (infractions.isEmpty()) {
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
                                if (infraction.punishment?.duration != null && infraction.punishment?.punishment !== PunishmentType.NONE)
                                    "for **" + timeToString(infraction.punishment?.duration!!) + "**" else "indefinitely"
                            }\n" +
                            infraction.reason
            )
        }

        if (strikes.isNotEmpty()) addField("", "**__Strikes__**")
        strikes.forEachIndexed { index, infraction ->
            addField(
                    "ID :: $index :: Weight :: ${infraction.points}",
                    "Type: **${infraction.type} (${infraction.points})** :: " +
                            "Date: **${SimpleDateFormat("dd/MM/yyyy").format(Date(infraction.dateTime))}**\n " +
                            "Punishment: **${infraction.punishment?.punishment}** ${
                                if (infraction.punishment?.duration != null && infraction.punishment?.punishment !== PunishmentType.NONE)
                                    "for **" + timeToString(infraction.punishment?.duration!!) + "**" else "indefinitely"
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
