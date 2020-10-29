package me.ddivad.judgebot.embeds

import com.gitlab.kordlib.core.entity.Guild
import com.gitlab.kordlib.core.entity.User
import com.gitlab.kordlib.rest.Image
import com.gitlab.kordlib.rest.builder.message.EmbedBuilder
import me.ddivad.judgebot.dataclasses.Infraction
import me.ddivad.judgebot.dataclasses.InfractionType
import me.ddivad.judgebot.dataclasses.Rule
import me.ddivad.judgebot.util.timeToString
import java.awt.Color

fun EmbedBuilder.createInfractionEmbed(guild: Guild, user: User, infraction: Infraction, rule: Rule?) {
    if (infraction.type == InfractionType.Warn) createWarnEmbed(guild, user, infraction)
    else if (infraction.type == InfractionType.Strike) createStrikeEmbed(guild, user, infraction, rule)
}

fun EmbedBuilder.createWarnEmbed(guild: Guild, user: User, infraction: Infraction) {
    title = "Warn"
    description = """
                    | ${user.mention}, you have received a **warning** from **${guild.name}**. A warning is a way for staff to inform you that your behaviour needs to change or further infractions will follow.
                    | If you think this to be unjustified, please **do not** post about it in a public channel but take it up with **Modmail**.
                """.trimMargin()

    field {
        name = "__Reason__"
        value = infraction.reason
        inline = false
    }
    color = Color.RED
    thumbnail {
        url = guild.getIconUrl(Image.Format.PNG) ?: ""
    }
}

fun EmbedBuilder.createStrikeEmbed(guild: Guild, user: User, infraction: Infraction, rule: Rule?) {
    title = "Strike"
    description = """
                    | ${user.mention}, you have received a **strike** from **${guild.name}**. A strike is a formal warning for breaking the rules.
                    | If you think this is unjustified, please **do not** post about it in a public channel but take it up with **Modmail**.
                """.trimMargin()

    if (infraction.ruleNumber != null) {
        field {
            name = "__Rule Broken__"
            value = "**[${rule?.title}](${rule?.link})** \n${rule?.description}"
        }
    }

    field {
        name = "__Reason__"
        value = infraction.reason
        inline = false
    }
    color = Color.RED
    thumbnail {
        url = guild.getIconUrl(Image.Format.PNG) ?: ""
    }
}

fun EmbedBuilder.createMuteEmbed(guild: Guild, user: User, reason: String, length: Long) {
    title = "Mute"
    description = """
                    | ${user.mention}, you have been muted. A muted user cannot speak/post in channels. 
                    | If you believe this to be in error, please contact Modmail.
                """.trimMargin()

    field {
        name = "Length"
        value = timeToString(length)
        inline = false
    }

    field {
        name = "__Reason__"
        value = reason
        inline = false
    }
    color = Color.RED
    thumbnail {
        url = guild.getIconUrl(Image.Format.PNG) ?: ""
    }
}