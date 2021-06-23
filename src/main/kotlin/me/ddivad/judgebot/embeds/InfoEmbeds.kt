package me.ddivad.judgebot.embeds

import dev.kord.common.kColor
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Member
import dev.kord.rest.Image
import dev.kord.rest.builder.message.EmbedBuilder
import me.ddivad.judgebot.dataclasses.*
import me.jakejmattson.discordkt.api.extensions.addField
import java.awt.Color

fun EmbedBuilder.createInformationEmbed(guild: Guild, user: Member, information: Info) {
    title = "Information"
    color = Color.CYAN.kColor
    thumbnail {
        url = guild.getIconUrl(Image.Format.PNG) ?: ""
    }
    description = """
                    ${user.mention}, the staff of **${guild.name}** have some information they want you to read. __This is not an infraction.__
                    Feel free to DM **Modmail** for more clarification.
                """.trimMargin()

    addField("Message", information.message)

    footer {
        icon = guild.getIconUrl(Image.Format.PNG) ?: ""
        text = guild.name
    }
}