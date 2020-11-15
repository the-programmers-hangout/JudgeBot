package me.ddivad.judgebot.embeds

import com.gitlab.kordlib.core.entity.Guild
import com.gitlab.kordlib.core.entity.Member
import com.gitlab.kordlib.core.entity.Message
import com.gitlab.kordlib.core.entity.User
import com.gitlab.kordlib.rest.Image
import com.gitlab.kordlib.rest.builder.message.EmbedBuilder
import me.ddivad.judgebot.dataclasses.*
import me.ddivad.judgebot.util.timeToString
import me.jakejmattson.discordkt.api.extensions.addField
import java.awt.Color

fun EmbedBuilder.createInfractionEmbed(guild: Guild, configuration: GuildConfiguration, user: User, guildMember: GuildMember, infraction: Infraction, rule: Rule?) {
    if (infraction.type == InfractionType.Warn) createWarnEmbed(guild, user, infraction)
    else if (infraction.type == InfractionType.Strike) createStrikeEmbed(guild, configuration, user, guildMember, infraction, rule)
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
    footer {
        icon = guild.getIconUrl(Image.Format.PNG) ?: ""
        text = guild.name
    }
}

fun EmbedBuilder.createStrikeEmbed(guild: Guild, configuration: GuildConfiguration, user: User, guildMember: GuildMember, infraction: Infraction, rule: Rule?) {
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

    field {
        name = "__Strike Points__"
        value = "${infraction.points}"
        inline = true
    }

    field {
        name = "__Points Count__"
        value = "${guildMember.getPoints(guild)} / ${configuration.infractionConfiguration.pointCeiling}"
        inline = true
    }

    field {
        name = "__Punishment__"
        value = "${infraction.punishment?.punishment.toString()} ${if (infraction.punishment?.duration != null) "for " + timeToString(infraction.punishment?.duration!!) else "indefinitely"}"
        inline = true
    }
    color = Color.RED
    thumbnail {
        url = guild.getIconUrl(Image.Format.PNG) ?: ""
    }
    footer {
        icon = guild.getIconUrl(Image.Format.PNG) ?: ""
        text = guild.name
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
    footer {
        icon = guild.getIconUrl(Image.Format.PNG) ?: ""
        text = guild.name
    }
}

fun EmbedBuilder.createUnmuteEmbed(guild: Guild, user: User) {
    color = Color.GREEN
    title = "Mute Removed"
    description = "${user.mention} you have been unmuted from **${guild.name}**."
    footer {
        icon = guild.getIconUrl(Image.Format.PNG) ?: ""
        text = guild.name
    }
}

fun EmbedBuilder.createBadPfpEmbed(guild: Guild, user: Member) {
    color = Color.RED
    thumbnail {
        url = guild.getIconUrl(Image.Format.PNG) ?: ""
    }
    title = "BadPfp"
    description = """
        ${user.mention}, we have flagged your profile picture as inappropriate. 
        Please change it within the next **30 minutes** or you will be banned.
    """.trimIndent()
    footer {
        icon = guild.getIconUrl(Image.Format.PNG) ?: ""
        text = guild.name
    }
}

fun EmbedBuilder.createMessageDeleteEmbed(guild: Guild, message: Message) {
    var messageContent = message.content.take(1010)
    if (message.content.length > 1024) messageContent += " ..."

    title = "Message Deleted"
    thumbnail {
        url = guild.getIconUrl(Image.Format.PNG) ?: ""
    }
    color = Color.RED
    description = """
        Your ${if (message.attachments.isNotEmpty()) "image" else "message"} was deleted from ${message.channel.mention} 
        as it is against our server rules.
    """.trimIndent()
    addField("Message", "```${messageContent}```")
    if (message.attachments.isNotEmpty()) {
        addField("Filename", "```${message.attachments.first().filename}```")
    }
    footer {
        icon = guild.getIconUrl(Image.Format.PNG) ?: ""
        text = guild.name
    }
}