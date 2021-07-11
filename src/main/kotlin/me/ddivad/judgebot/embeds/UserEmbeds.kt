package me.ddivad.judgebot.embeds

import dev.kord.common.entity.Snowflake
import dev.kord.common.kColor
import dev.kord.core.entity.Guild
import dev.kord.core.entity.User
import dev.kord.rest.Image
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.x.emoji.Emojis
import kotlinx.datetime.toJavaInstant
import me.ddivad.judgebot.dataclasses.*
import me.ddivad.judgebot.services.DatabaseService
import me.ddivad.judgebot.util.*
import me.jakejmattson.discordkt.api.dsl.MenuBuilder
import me.jakejmattson.discordkt.api.extensions.addField
import me.jakejmattson.discordkt.api.extensions.addInlineField
import me.jakejmattson.discordkt.api.extensions.toSnowflake
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
    val userRecord = member.getGuildInfo(guild.id.asString)
    val paginatedNotes = userRecord.notes.chunked(4)

    val totalMenuPages = 1 + 1 + 1 + 1 + if (paginatedNotes.isNotEmpty()) paginatedNotes.size else 1
    val guildConfiguration = config[guild.id.value]!!
    val embedColor = getEmbedColour(guild, target, databaseService)
    val leaveData = databaseService.joinLeaves.getMemberJoinLeaveDataForGuild(guild.id.asString, member.userId)
    this.apply {
        buildOverviewPage(guild, guildConfiguration, target, userRecord, embedColor, totalMenuPages, databaseService)
        buildInfractionPage(guild, guildConfiguration, target, userRecord, embedColor, totalMenuPages)
        buildNotesPages(guild, guildConfiguration, target, userRecord, embedColor, totalMenuPages)
        buildInformationPage(guild, guildConfiguration, target, userRecord, embedColor, totalMenuPages)
        buildJoinLeavePage(guild, target, leaveData, userRecord, embedColor, totalMenuPages)
    }

    buttons {
        button("Overview", Emojis.clipboard) {
            loadPage(0)
        }
        button("Infractions", Emojis.warning) {
            loadPage(1)
        }
        button("Notes", Emojis.pencil) {
            loadPage(2)
        }
        button("Info", Emojis.informationSource) {
            loadPage(3 + if (paginatedNotes.isNotEmpty()) paginatedNotes.size - 1 else 0)
        }
        button("Leaves", Emojis.x) {
            loadPage(4 + if (paginatedNotes.isNotEmpty()) paginatedNotes.size - 1 else 0)
        }
    }

    if ((paginatedNotes.size > 1)) {
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

private suspend fun MenuBuilder.buildOverviewPage(
    guild: Guild,
    config: GuildConfiguration,
    target: User,
    userRecord: GuildMemberDetails,
    embedColor: Color,
    totalPages: Int,
    databaseService: DatabaseService
) {
    page {
        color = embedColor.kColor
        title = "${target.asUser().tag}: Overview"
        thumbnail {
            url = target.asUser().avatar.url
        }
        val memberInGuild = target.asMemberOrNull(guild.id)

        addInlineField(
            "Record",
            """
                **${userRecord.infractions.size}** Infraction(s)
                **${userRecord.notes.size}** Note(s)
                **${userRecord.info.size}** Information(s)
                **${userRecord.deletedMessageCount.deleteReaction}** Deletes (${config.reactions.deleteMessageReaction})
            """.trimIndent()
        )
        addInlineField("Points", "**${userRecord.points} / ${config.infractionConfiguration.pointCeiling}**")
        addInlineField("History Invokes", "${userRecord.historyCount}")

        addInlineField("Created", formatOffsetTime(target.id.timeStamp.toJavaInstant()))
        if (memberInGuild != null) {
            addInlineField("Joined", formatOffsetTime(memberInGuild.joinedAt.toJavaInstant()))
        } else addInlineField("", "")

        if (userRecord.linkedAccounts.isNotEmpty()) {
            addInlineField(
                "Alts",
                userRecord.linkedAccounts.map { guild.kord.getUser(Snowflake(it))?.mention }.joinToString("\n")
            )
        }

        getStatus(guild, target, databaseService)?.let { addField("Status", it) }

        if (userRecord.infractions.size > 0) {
            val lastInfraction = userRecord.infractions.maxByOrNull { it.dateTime }!!

            addField(
                "**__Most Recent Infraction__**",
                "Type: **${lastInfraction.type} (${lastInfraction.points})** Rule: **${lastInfraction.ruleNumber ?: "None"}**\n " +
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
    config: GuildConfiguration,
    target: User,
    userRecord: GuildMemberDetails,
    embedColor: Color,
    totalPages: Int
) {
    page {
        color = embedColor.kColor
        title = "${target.asUser().tag}: Infractions"
        thumbnail {
            url = target.asUser().avatar.url
        }
        val warnings = userRecord.infractions.filter { it.type == InfractionType.Warn }.sortedBy { it.dateTime }
        val strikes = userRecord.infractions.filter { it.type == InfractionType.Strike }.sortedBy { it.dateTime }

        addInlineField("Points", "**${userRecord.points} / ${config.infractionConfiguration.pointCeiling}**")
        addInlineField("Warns", "${warnings.size}")
        addInlineField("Strikes", "${strikes.size}")

        if (userRecord.infractions.isEmpty()) addField("", "**User has no infractions**")
        if (warnings.isNotEmpty()) addField("", "**__Warnings__**")
        warnings.forEachIndexed { _, infraction ->
            val moderator = guild.kord.getUser(Snowflake(infraction.moderator))?.username
            addField(
                "ID :: ${infraction.id} :: Staff :: __${moderator}__",
                "Type: **${infraction.type} (${infraction.points})** :: " +
                        "Date: **${SimpleDateFormat("dd/MM/yyyy").format(Date(infraction.dateTime))}**\n " +
                        "Rule: **${infraction.ruleNumber ?: "None"}**\n" +
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
                        "Rule: **${infraction.ruleNumber ?: "None"}**\n" +
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
    val paginatedNotes = userRecord.notes.sortedBy { it.dateTime }.chunked(4)
    if (userRecord.notes.isEmpty()) {
        page {
            color = embedColor.kColor
            title = "${target.asUser().tag}: Notes"
            thumbnail {
                url = target.asUser().avatar.url
            }

            addInlineField("Points", "**${userRecord.points} / ${config.infractionConfiguration.pointCeiling}**")
            addInlineField("Notes", "${userRecord.notes.size}")
            addInlineField("Information", "${userRecord.info.size}")

            addField("", "**User has no notes**")
            footer {
                icon = guild.getIconUrl(Image.Format.PNG) ?: ""
                text = "Page 3 of $totalPages"
            }
        }
    }

    paginatedNotes.forEachIndexed { index, list ->
        page {
            color = embedColor.kColor
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
        color = embedColor.kColor
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
    joinLeaves: List<JoinLeave>,
    userRecord: GuildMemberDetails,
    embedColor: Color,
    totalPages: Int
) {
    page {
        val leaves = joinLeaves.filter { it.leaveDate != null }
        val paginatedNotes = userRecord.notes.chunked(4)

        color = embedColor.kColor
        title = "${target.asUser().tag}: Join / Leave"
        thumbnail {
            url = target.asUser().avatar.url
        }

        addInlineField("Joins:", joinLeaves.size.toString())
        addInlineField("", "")
        addInlineField("Leaves:", leaves.size.toString())
        addField("", "")
        joinLeaves.forEachIndexed { index, record ->
            addInlineField("Record", "#${index + 1}")
            addInlineField("Joined", SimpleDateFormat("dd/MM/yyyy").format(Date(record.joinDate)))
            addInlineField(
                "Left", if (record.leaveDate == null) "-" else SimpleDateFormat("dd/MM/yyyy").format(
                    Date(
                        record.leaveDate!!
                    )
                )
            )

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
        val reason = databaseService.guilds.getBanOrNull(guild, target.id.asString)?.reason ?: it.reason
        return "```css\nUser is banned with reason:\n${reason}```"
    }
    if (target.asMemberOrNull(guild.id) == null) return "```css\nUser not currently in this guild```"
    databaseService.guilds.getPunishmentsForUser(guild, target).firstOrNull()?.let {
        return "```css\nActive ${it.type} with ${timeBetween(DateTime(it.clearTime))} left```"
    }
    return null
}

suspend fun MenuBuilder.createLinkedAccountMenu(
    linkedAccountIds: List<String>,
    guild: Guild,
    config: Configuration,
    databaseService: DatabaseService
) {
    linkedAccountIds.forEach {
        val linkedUser = guild.kord.getUser(it.toSnowflake()) ?: return@forEach
        val linkedUserRecord = databaseService.users.getOrCreateUser(linkedUser, guild)
        page {
            createCondensedHistoryEmbed(linkedUser, linkedUserRecord, guild, config)
        }
    }

    buttons {
        button("Prev.", Emojis.arrowLeft) {
            previousPage()
        }
        button("Next", Emojis.arrowRight) {
            nextPage()
        }
    }
}

suspend fun EmbedBuilder.createCondensedHistoryEmbed(
    target: User,
    member: GuildMember,
    guild: Guild,
    config: Configuration
) {

    val userGuildDetails = member.getGuildInfo(guild.id.asString)
    val infractions = userGuildDetails.infractions
    val warnings = userGuildDetails.infractions.filter { it.type == InfractionType.Warn }
    val strikes = userGuildDetails.infractions.filter { it.type == InfractionType.Strike }
    val notes = userGuildDetails.notes
    val maxPoints = config[guild.id.value]?.infractionConfiguration?.pointCeiling

    color = Color.MAGENTA.kColor
    title = "${target.asUser().tag}'s Record"
    thumbnail {
        url = target.asUser().avatar.url
    }
    addInlineField("Infractions", "${infractions.size}")
    addInlineField("Notes", "${notes.size}")
    addInlineField("Points", "**${member.getPoints(guild)} / $maxPoints**")

    if (notes.isEmpty()) {
        addField("", "**__Notes__**")
        addField("No notes recorded.", "")
    } else {
        addField("", "**__Notes__**")
        notes.forEach { note ->
            val moderator = guild.kord.getUser(Snowflake(note.moderator))?.username

            addField(
                "ID :: ${note.id} :: Staff :: __${moderator}__",
                "Noted by **${moderator}** on **${SimpleDateFormat("dd/MM/yyyy").format(Date(note.dateTime))}**\n" +
                        note.note
            )
        }
    }

    if (infractions.isEmpty()) {
        addField("", "**__Infractions__**")
        addField("No infractions issued.", "")
    } else {
        if (warnings.isNotEmpty()) addField("", "**__Warnings__**")
        warnings.forEachIndexed { index, infraction ->
            val moderator = guild.kord.getUser(Snowflake(infraction.moderator))?.username
            addField(
                "ID :: $index :: Staff :: $moderator",
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
            val moderator = guild.kord.getUser(Snowflake(infraction.moderator))?.username
            addField(
                "ID :: $index :: Staff :: $moderator",
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

suspend fun EmbedBuilder.createSelfHistoryEmbed(
    target: User,
    member: GuildMember,
    guild: Guild,
    config: Configuration
) {

    val userGuildDetails = member.getGuildInfo(guild.id.asString)
    val infractions = userGuildDetails.infractions
    val warnings = userGuildDetails.infractions.filter { it.type == InfractionType.Warn }
    val strikes = userGuildDetails.infractions.filter { it.type == InfractionType.Strike }
    val maxPoints = config[guild.id.value]?.infractionConfiguration?.pointCeiling

    color = Color.MAGENTA.kColor
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
