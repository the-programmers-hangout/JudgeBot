package me.ddivad.judgebot.embeds

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.entity.Guild
import com.gitlab.kordlib.core.entity.Member
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.dataclasses.GuildMember
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
        target: Member,
        member: GuildMember,
        guild: Guild,
        config: Configuration,
        includeModerator: Boolean,
) {
    val userGuildDetails = member.getGuildInfo(guild.id.value)!!
    val notes = userGuildDetails.notes
    val infractions = userGuildDetails.infractions
    val paginatedNotes = notes.chunked(5)
    page {
        color = Color.MAGENTA
        title = "${target.asUser().tag}'s Record"
        thumbnail {
            url = target.asUser().avatar.url
        }

        addInlineField("Notes", "${notes.size}")
        addInlineField("Infractions", "${infractions.size}")
        addInlineField("Status", "TBI")
        addInlineField("Join date", formatOffsetTime(target.joinedAt))
        addInlineField("Creation date", formatOffsetTime(target.id.timeStamp))
        addInlineField("History Invokes", "${userGuildDetails.historyCount}")
        addField("", "")
        if (infractions.size > 0) {
            val lastInfraction = userGuildDetails.infractions[userGuildDetails.infractions.size - 1]
            addField(
                    "**__Most Recent Infraction__**",
                    "Type: **${lastInfraction.type}** :: Weight: **${lastInfraction.points}**\n " +
                            "Issued by **${guild.kord.getUser(Snowflake(lastInfraction.moderator))?.username}** " +
                            "on **${SimpleDateFormat("dd/MM/yyyy").format(Date(lastInfraction.dateTime))}**\n" +
                            lastInfraction.reason
            )
        } else addField("User has no recent infractions.", "")

    }

    if (notes.isEmpty()) {
        page {
            color = Color.MAGENTA
            title = "${target.asUser().tag}'s Record"
            thumbnail {
                url = target.asUser().avatar.url
            }

            addInlineField("Notes", "${notes.size}")
            addInlineField("Infractions", "${infractions.size}")
            addInlineField("Status", "TBI")
            addInlineField("Join date", formatOffsetTime(target.joinedAt))
            addInlineField("Creation date", formatOffsetTime(target.id.timeStamp))
            addInlineField("History Invokes", "${userGuildDetails.historyCount}")

            addField("", "**__Notes:__**")
            addField("**No Notes**", "User has no notes written.")
        }
    }

    paginatedNotes.forEachIndexed { index, list ->
        page {
            color = Color.MAGENTA
            title = "${target.asUser().tag}'s Record"
            thumbnail {
                url = target.asUser().avatar.url
            }

            addInlineField("Notes", "${notes.size}")
            addInlineField("Infractions", "${infractions.size}")
            addInlineField("Status", "TBI")
            addInlineField("Join date", formatOffsetTime(target.joinedAt))
            addInlineField("Creation date", formatOffsetTime(target.id.timeStamp))
            addInlineField("History Invokes", "${userGuildDetails.historyCount}")

            addField("", "**__Notes:__**")
            list.forEachIndexed { index, note ->
                val moderator = guild.kord.getUser(Snowflake(note.moderator))?.username

                addField(
                        "ID :: ${note.id} :: Staff :: __${moderator}__",
                        "Noted by **${moderator}** on **${SimpleDateFormat("dd/MM/yyyy").format(Date(note.dateTime))}**\n" +
                                note.note
                )
            }
        }
    }
}

suspend fun CommandEvent<*>.createStatusEmbed(target: Member,
                                              member: GuildMember,
                                              guild: Guild) = respond {
    val userGuildDetails = member.getGuildInfo(guild.id.value)!!
    val notes = userGuildDetails.notes
    val infractions = userGuildDetails.infractions

    color = discord.configuration.theme
    title = "${target.asUser().tag}'s Record"
    thumbnail {
        url = target.asUser().avatar.url
    }

    addInlineField("Notes", "${notes.size}")
    addInlineField("Infractions", "${infractions.size}")
    addInlineField("Status", "TBI")
    addInlineField("Join date", formatOffsetTime(target.joinedAt))
    addInlineField("Creation date", formatOffsetTime(target.id.timeStamp))
    addInlineField("History Invokes", "${userGuildDetails.historyCount}")
}

private fun formatOffsetTime(time: Instant): String {
    val days = TimeUnit.MILLISECONDS.toDays(DateTime.now().millis - time.toEpochMilli())
    val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(Locale.UK).withZone(ZoneOffset.UTC);
    return if (days > 4) {
        "$days days ago\n${formatter.format(time)}"
    } else {
        val hours = TimeUnit.MILLISECONDS.toHours(DateTime.now().millis - time.toEpochMilli())
        "$hours hours ago\n${formatter.format(time)}"
    }
}
