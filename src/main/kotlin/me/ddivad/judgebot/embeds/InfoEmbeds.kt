package me.ddivad.judgebot.embeds

import com.gitlab.kordlib.core.entity.Guild
import com.gitlab.kordlib.core.entity.Member
import com.gitlab.kordlib.core.entity.Message
import com.gitlab.kordlib.core.entity.User
import com.gitlab.kordlib.rest.Image
import com.gitlab.kordlib.rest.builder.message.EmbedBuilder
import me.ddivad.judgebot.dataclasses.*
import me.ddivad.judgebot.extensions.jumpLink
import me.jakejmattson.discordkt.api.extensions.addField
import me.jakejmattson.discordkt.api.extensions.addInlineField
import java.awt.Color

fun EmbedBuilder.createInformationEmbed(guild: Guild, user: Member, information: Info) {
    title = "Information"
    color = Color.CYAN
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