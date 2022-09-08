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
import me.ddivad.judgebot.util.timeToString
import me.jakejmattson.discordkt.dsl.MenuBuilder
import me.jakejmattson.discordkt.extensions.*
import java.awt.Color
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

suspend fun MenuBuilder.createHistoryEmbed(
    target: User,
    member: GuildMember,
    guild: Guild,
    config: Configuration,
    databaseService: DatabaseService
) {
    val userRecord = member.getGuildInfo(guild.id.toString())
    val paginatedNotes = userRecord.notes.chunked(4)

    val totalMenuPages = 1 + 1 + 1 + 1 + if (paginatedNotes.isNotEmpty()) paginatedNotes.size else 1
    val guildConfiguration = config[guild.id]!!
    val embedColor = getEmbedColour(guild, target, databaseService)
    val leaveData = databaseService.joinLeaves.getMemberJoinLeaveDataForGuild(guild.id.toString(), member.userId)
    this.apply {
        buildOverviewPage(guild, guildConfiguration, target, userRecord, embedColor, totalMenuPages, databaseService)
        buildInfractionPage(guild, guildConfiguration, target, userRecord, embedColor, totalMenuPages)
        buildNotesPages(guild, guildConfiguration, target, userRecord, embedColor, totalMenuPages)
        buildInformationPage(guild, guildConfiguration, target, userRecord, embedColor, totalMenuPages)
        buildJoinLeavePage(guild, target, leaveData, userRecord, embedColor, totalMenuPages)
    }

    buttons {
        button(
            "Overview (${userRecord.points} / ${guildConfiguration.infractionConfiguration.pointCeiling})",
            Emojis.clipboard
        ) {
            loadPage(0)
        }
        button("Infractions (${userRecord.infractions.size})", Emojis.warning) {
            loadPage(1)
        }
        button("Notes (${userRecord.notes.size})", Emojis.pencil) {
            loadPage(2)
        }
        button("Messages (${userRecord.info.size})", Emojis.informationSource) {
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
        title = "User Overview (${userRecord.points} / ${config.infractionConfiguration.pointCeiling})"
        thumbnail {
            url = target.asUser().pfpUrl
        }
        val memberInGuild = target.asMemberOrNull(guild.id)

        addInlineField("Infractions", "**${userRecord.infractions.size}**")
        addInlineField("Notes", "**${userRecord.notes.size}**")
        addInlineField("Messages", "**${userRecord.info.size}**")
        addInlineField("History", "${userRecord.historyCount}")
        addInlineField("Created", TimeStamp.at(target.id.timestamp.toJavaInstant(), TimeStyle.RELATIVE))

        if (memberInGuild != null) {
            addInlineField("Joined", TimeStamp.at(memberInGuild.joinedAt.toJavaInstant(), TimeStyle.RELATIVE))
        } else addInlineField("", "")

        getStatus(guild, target, databaseService)?.let { addField("Current Status", it) }

        if (userRecord.infractions.size > 0) {
            val lastInfraction = userRecord.infractions.maxByOrNull { it.dateTime }!!
            addField(
                "**__Most Recent Infraction__**: (${
                    TimeStamp.at(
                        Instant.ofEpochMilli(lastInfraction.dateTime),
                        TimeStyle.RELATIVE
                    )
                })",
                "Type: **${lastInfraction.type} (${lastInfraction.points})** Rule: **${lastInfraction.ruleNumber ?: "None"}**\n " +
                        "Issued by **${guild.kord.getUser(Snowflake(lastInfraction.moderator))?.username}** " +
                        "on **${SimpleDateFormat("dd/MM/yyyy").format(Date(lastInfraction.dateTime))}**\n" +
                        "Punishment: **${lastInfraction.punishment?.punishment}** ${getDurationText(lastInfraction.punishment)}\n" +
                        lastInfraction.reason
            )
        } else addField("", "**User has no recent infractions**")

        addInlineField(
            "",
            "**${userRecord.deletedMessageCount.deleteReaction}** Deletes (${config.reactions.deleteMessageReaction})"
        )
        addInlineField("", "**${userRecord.bans.size}** Bans (${Emojis.x})")
        addInlineField("", "**${userRecord.linkedAccounts.size}** Alt(s) (${Emojis.link})")

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
        title = "Infractions"
        thumbnail {
            url = target.asUser().pfpUrl
        }
        val warnings = userRecord.infractions.filter { it.type == InfractionType.Warn }.sortedBy { it.dateTime }
        val strikes = userRecord.infractions.filter { it.type == InfractionType.Strike }.sortedBy { it.dateTime }

        addInlineField("Current Points", "**${userRecord.points} / ${config.infractionConfiguration.pointCeiling}**")
        addInlineField("Infraction Count", "${userRecord.infractions.size}")
        addInlineField("Total Points", "${userRecord.infractions.sumOf { it.points }}")


        if (userRecord.infractions.isEmpty()) addField("", "**User has no infractions**")
        if (warnings.isNotEmpty()) addField("", "**__Warnings  (${warnings.size})__**")
        warnings.forEachIndexed { _, infraction ->
            val moderator = guild.kord.getUser(Snowflake(infraction.moderator))?.username
            addField(
                "ID :: ${infraction.id} :: Staff :: __${moderator}__",
                "Type: **${infraction.type} (${infraction.points})** :: " +
                        "Rule: **${infraction.ruleNumber ?: "None"}**\n" +
                        "Date: **${SimpleDateFormat("dd/MM/yyyy").format(Date(infraction.dateTime))}** (${
                            TimeStamp.at(
                                Instant.ofEpochMilli(infraction.dateTime),
                                TimeStyle.RELATIVE
                            )
                        })\n " +
                        "Punishment: **${infraction.punishment?.punishment}** ${getDurationText(infraction.punishment)}\n" +
                        infraction.reason
            )
        }

        if (strikes.isNotEmpty()) addField("", "**__Strikes (${strikes.size})__**")
        strikes.forEachIndexed { _, infraction ->
            val moderator = guild.kord.getUser(Snowflake(infraction.moderator))?.username
            addField(
                "ID :: ${infraction.id} :: Staff :: __${moderator}__",
                "Type: **${infraction.type} (${infraction.points})** :: " +
                        "Rule: **${infraction.ruleNumber ?: "None"}**\n" +

                        "Date: **${SimpleDateFormat("dd/MM/yyyy").format(Date(infraction.dateTime))}** (${
                            TimeStamp.at(
                                Instant.ofEpochMilli(infraction.dateTime),
                                TimeStyle.RELATIVE
                            )
                        })\n " +
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
            title = "Notes"
            thumbnail {
                url = target.asUser().pfpUrl
            }

            addInlineField("Points", "**${userRecord.points} / ${config.infractionConfiguration.pointCeiling}**")
            addInlineField("Notes", "${userRecord.notes.size}")
            addInlineField("Messages", "${userRecord.info.size}")

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
            title = "Notes" + if (paginatedNotes.size > 1) "(${index + 1})" else ""
            thumbnail {
                url = target.asUser().pfpUrl
            }
            addInlineField("Notes", "${userRecord.notes.size}")
            addInlineField("Messages", "${userRecord.info.size}")
            addInlineField("Points", "**${userRecord.points} / ${config.infractionConfiguration.pointCeiling}**")

            list.forEachIndexed { _, note ->
                val moderator = guild.kord.getUser(Snowflake(note.moderator))?.username ?: note.moderator
                addField(
                    "ID :: ${note.id} :: Staff :: __${moderator}__",
                    "Date: **${SimpleDateFormat("dd/MM/yyyy").format(Date(note.dateTime))}** (${
                        TimeStamp.at(
                            Instant.ofEpochMilli(
                                note.dateTime
                            ), TimeStyle.RELATIVE
                        )
                    })\n" +
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
        title = "Messages"
        thumbnail {
            url = target.asUser().pfpUrl
        }
        addInlineField("Notes", "${userRecord.notes.size}")
        addInlineField("Messages", "${userRecord.info.size}")
        addInlineField("Points", "**${userRecord.points} / ${config.infractionConfiguration.pointCeiling}**")

        if (userRecord.info.isEmpty()) addField("", "**User has no message records**")
        userRecord.info.forEachIndexed { _, info ->
            val moderator = guild.kord.getUser(Snowflake(info.moderator))?.username
            addField(
                "ID :: ${info.id} :: Staff :: __${moderator}__",
                "Date: **${SimpleDateFormat("dd/MM/yyyy").format(Date(info.dateTime))}** (${
                    TimeStamp.at(
                        Instant.ofEpochMilli(
                            info.dateTime
                        ), TimeStyle.RELATIVE
                    )
                })\n" +
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
        title = "Join / Leave"
        thumbnail {
            url = target.asUser().pfpUrl
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
            ""
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
    var status = ""
    val userRecord = databaseService.users.getOrCreateUser(target, guild).getGuildInfo(guild.id.toString())
    guild.getBanOrNull(target.id)?.let {
        val reason = databaseService.guilds.getBanOrNull(guild, target.id.toString())?.reason ?: it.reason
        return "```css\nUser is banned with reason:\n${reason}```"
    }
    if (target.asMemberOrNull(guild.id) == null) return "```css\nUser not currently in this guild```"
    if (userRecord.pointDecayFrozen) {
        status += "```css\nPoint decay is currently frozen for this user```"
    }
    if (userRecord.bans.lastOrNull()?.thinIce == true && userRecord.pointDecayFrozen && Instant.now()
            .toEpochMilli() < userRecord.pointDecayTimer
    ) {
        status += "User is on Thin Ice after being unbanned on ${
            userRecord.bans.last().unbanTime?.let {
                Instant.ofEpochMilli(
                    it
                )
            }?.let { TimeStamp.at(it) }
        }. Point decay frozen until ${TimeStamp.at(Instant.ofEpochMilli(userRecord.pointDecayTimer))}"
    }
    databaseService.guilds.getPunishmentsForUser(guild, target).firstOrNull()?.let {
        val clearTime = Instant.ofEpochMilli(it.clearTime!!)
        status += "\nMuted until ${TimeStamp.at(clearTime)} (${TimeStamp.at(clearTime, TimeStyle.RELATIVE)})"
    }
    return status
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

    val userGuildDetails = member.getGuildInfo(guild.id.toString())
    val infractions = userGuildDetails.infractions
    val warnings = userGuildDetails.infractions.filter { it.type == InfractionType.Warn }
    val strikes = userGuildDetails.infractions.filter { it.type == InfractionType.Strike }
    val notes = userGuildDetails.notes
    val maxPoints = config[guild.id]?.infractionConfiguration?.pointCeiling

    color = Color.MAGENTA.kColor
    title = "User Overview (${userGuildDetails.points} / $maxPoints)"
    thumbnail {
        url = target.asUser().pfpUrl
    }
    val memberInGuild = target.asMemberOrNull(guild.id)

    addInlineField("Infractions", "**${userGuildDetails.infractions.size}**")
    addInlineField("Notes", "**${userGuildDetails.notes.size}**")
    addInlineField("Info", "**${userGuildDetails.info.size}**")
    addInlineField("History", "${userGuildDetails.historyCount}")
    addInlineField("Created", TimeStamp.at(target.id.timestamp.toJavaInstant(), TimeStyle.RELATIVE))

    if (memberInGuild != null) {
        addInlineField("Joined", TimeStamp.at(memberInGuild.joinedAt.toJavaInstant(), TimeStyle.RELATIVE))
    } else addInlineField("", "")

    if (notes.isEmpty()) {
        addField("", "**__Notes__**")
        addField("No notes recorded.", "")
    } else {
        addField("", "**__Notes__**")
        notes.forEach { note ->
            val moderator = guild.kord.getUser(Snowflake(note.moderator))?.username

            addField(
                "ID :: ${note.id} :: Staff :: __${moderator}__",
                "Noted by **${moderator}** on **${SimpleDateFormat("dd/MM/yyyy").format(Date(note.dateTime))}** (${
                    TimeStamp.at(
                        Instant.ofEpochMilli(note.dateTime),
                        TimeStyle.RELATIVE
                    )
                })\n" +
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
                        "Date: **${SimpleDateFormat("dd/MM/yyyy").format(Date(infraction.dateTime))}** (${
                            TimeStamp.at(
                                Instant.ofEpochMilli(infraction.dateTime),
                                TimeStyle.RELATIVE
                            )
                        })\n " +
                        "Punishment: **${infraction.punishment?.punishment}** ${
                            if (infraction.punishment?.duration != null && infraction.punishment?.punishment !== PunishmentType.NONE)
                                "for **" + timeToString(infraction.punishment?.duration!!) + "**" else ""
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
                        "Date: **${SimpleDateFormat("dd/MM/yyyy").format(Date(infraction.dateTime))}** (${
                            TimeStamp.at(
                                Instant.ofEpochMilli(infraction.dateTime),
                                TimeStyle.RELATIVE
                            )
                        })\n " +
                        "Punishment: **${infraction.punishment?.punishment}** ${
                            if (infraction.punishment?.duration != null && infraction.punishment?.punishment !== PunishmentType.NONE)
                                "for **" + timeToString(infraction.punishment?.duration!!) + "**" else ""
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
    val userGuildDetails = member.getGuildInfo(guild.id.toString())
    val infractions = userGuildDetails.infractions
    val warnings = userGuildDetails.infractions.filter { it.type == InfractionType.Warn }
    val strikes = userGuildDetails.infractions.filter { it.type == InfractionType.Strike }
    val maxPoints = config[guild.id]?.infractionConfiguration?.pointCeiling

    color = Color.MAGENTA.kColor
    title = "${target.asUser().tag}'s Record"
    thumbnail {
        url = target.asUser().pfpUrl
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
                        "Date: **${SimpleDateFormat("dd/MM/yyyy").format(Date(infraction.dateTime))}** (${
                            TimeStamp.at(
                                Instant.ofEpochMilli(infraction.dateTime), TimeStyle.RELATIVE
                            )
                        })\n " +
                        "Punishment: **${infraction.punishment?.punishment}** ${
                            if (infraction.punishment?.duration != null && infraction.punishment?.punishment !== PunishmentType.NONE)
                                "for **" + timeToString(infraction.punishment?.duration!!) + "**" else ""
                        }\n" +
                        infraction.reason
            )
        }

        if (strikes.isNotEmpty()) addField("", "**__Strikes__**")
        strikes.forEachIndexed { index, infraction ->
            addField(
                "ID :: $index :: Weight :: ${infraction.points}",
                "Type: **${infraction.type} (${infraction.points})** :: " +
                        "Date: **${SimpleDateFormat("dd/MM/yyyy").format(Date(infraction.dateTime))}** (${
                            TimeStamp.at(
                                Instant.ofEpochMilli(infraction.dateTime), TimeStyle.RELATIVE
                            )
                        })\n " +
                        "Punishment: **${infraction.punishment?.punishment}** ${
                            if (infraction.punishment?.duration != null && infraction.punishment?.punishment !== PunishmentType.NONE)
                                "for **" + timeToString(infraction.punishment?.duration!!) + "**" else ""
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
