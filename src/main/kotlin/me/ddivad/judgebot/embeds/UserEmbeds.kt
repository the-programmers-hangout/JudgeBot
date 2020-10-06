package me.ddivad.judgebot.embeds

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.entity.Guild
import com.gitlab.kordlib.core.entity.Member
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.dataclasses.GuildMember
import me.jakejmattson.discordkt.api.dsl.CommandEvent
import me.jakejmattson.discordkt.api.extensions.addField
import me.jakejmattson.discordkt.api.extensions.addInlineField
import java.text.SimpleDateFormat
import java.util.*

suspend fun CommandEvent<*>.createHistoryEmbed(target: Member,
                                               member: GuildMember,
                                               guild: Guild,
                                               config: Configuration,
                                               includeModerator: Boolean): Unit = respondMenu {
    val userGuildDetails = member.getGuildInfo(guild.id.value)!!
    val notes = userGuildDetails.notes
    val infractions = userGuildDetails.infractions
    val paginatedNotes = notes.chunked(5)
    val lastInfraction = userGuildDetails.infractions[userGuildDetails.infractions.size - 1]
    page {
        color = discord.configuration.theme
        title = "${target.asUser().tag}'s Record"
        thumbnail {
            url = target.asUser().avatar.url
        }

        addInlineField("Notes", "${notes.size}")
        addInlineField("Infractions", "${infractions.size}")
        addInlineField("Status", "TBI")
        addInlineField("Join date", target.joinedAt.toString())
        addInlineField("Creation date", target.asUser().id.timeStamp.toString())
        addInlineField("History Invokes", "${userGuildDetails.historyCount}")
        addField("", "")
        addField(
                "**__Most Recent Infraction__**",
                "Type: **${lastInfraction.weight}** :: Weight: **${lastInfraction.strikes}**\n " +
                        "Issued by **${guild.kord.getUser(Snowflake(lastInfraction.moderator))?.username}** " +
                        "on **${SimpleDateFormat("dd/MM/yyyy").format(Date(lastInfraction.dateTime))}**\n" +
                        lastInfraction.reason
        )
    }

    if (notes.isEmpty()) {
        page {
            color = discord.configuration.theme
            title = "${target.asUser().tag}'s Record"
            thumbnail {
                url = target.asUser().avatar.url
            }

            addInlineField("Notes", "${notes.size}")
            addInlineField("Infractions", "${infractions.size}")
            addInlineField("Status", "TBI")
            addInlineField("Join date", target.joinedAt.toString())
            addInlineField("Creation date", target.asUser().id.timeStamp.toString())
            addInlineField("History Invokes", "${userGuildDetails.historyCount}")

            addField("", "**__Notes:__**")
            addField("**No Notes**", "User has no notes written.")
        }
    }

    paginatedNotes.forEachIndexed { index, list ->
        page {
            color = discord.configuration.theme
            title = "${target.asUser().tag}'s Record"
            thumbnail {
                url = target.asUser().avatar.url
            }

            addInlineField("Notes", "${notes.size}")
            addInlineField("Infractions", "${infractions.size}")
            addInlineField("Status", "TBI")
            addInlineField("Join date", target.joinedAt.toString())
            addInlineField("Creation date", target.asUser().id.timeStamp.toString())
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

    color = discord.configuration.theme
    title = "${target.asUser().tag}'s Record"
    thumbnail {
        url = target.asUser().avatar.url
    }

    addInlineField("Notes", "${notes.size}")
    addInlineField("Infractions", "0")
    addInlineField("Status", "TBI")
    addInlineField("Join date", target.joinedAt.toString())
    addInlineField("Creation date", target.asUser().id.timeStamp.toString())
    addInlineField("History Invokes", "${userGuildDetails.historyCount}")
}
