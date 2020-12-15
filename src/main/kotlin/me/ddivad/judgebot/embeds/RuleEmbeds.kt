package me.ddivad.judgebot.embeds

import com.gitlab.kordlib.core.entity.Guild
import com.gitlab.kordlib.rest.builder.message.EmbedBuilder
import me.ddivad.judgebot.dataclasses.Rule
import me.jakejmattson.discordkt.api.extensions.addField
import java.awt.Color
import com.gitlab.kordlib.rest.Image

fun EmbedBuilder.createRuleEmbed(guild: Guild, rule: Rule) {
    title = "__${rule.number}: ${rule.title}__"
    description = rule.description
    thumbnail {
        url = guild.getIconUrl(Image.Format.PNG) ?: ""
    }
    if (rule.link !== "") {
        addField("", "[View this on our website](${rule.link})")
    }
    color = Color.MAGENTA
    footer {
        icon = guild.getIconUrl(Image.Format.PNG) ?: ""
        text = guild.name
    }
}

fun EmbedBuilder.createRulesEmbed(guild: Guild, rules: List<Rule>) {
    title = "**__Server Rules__**"
    thumbnail {
        url = guild.getIconUrl(Image.Format.PNG) ?: ""
    }
    color = Color.MAGENTA

    field {
        for (rule in rules) {
            value += "**[${rule.number}](${rule.link})**. ${rule.title}\n"
        }
    }
    footer {
        icon = guild.getIconUrl(Image.Format.PNG) ?: ""
        text = guild.name
    }
}

fun EmbedBuilder.createInfractionRuleEmbed(guild: Guild, rules: List<Rule>) {
    title = "**__Available Rules__**"
    color = Color.MAGENTA
    description = ""
    for (rule in rules) {
        description += "**[${rule.number}](${rule.link})**. ${rule.title}\n"
    }

    footer {
        icon = guild.getIconUrl(Image.Format.PNG) ?: ""
        text = guild.name
    }
    addField("","")
    addField("Rule Choice:", "Reply with **rule id**, or **0** for no rule.")
}

fun EmbedBuilder.createRulesEmbedDetailed(guild: Guild, rules: List<Rule>) {
    title = "**__Server Rules__**"
    thumbnail {
        url = guild.getIconUrl(Image.Format.PNG) ?: ""
    }
    color = Color.MAGENTA

    for (rule in rules) {
        field {
            value = if (rule.link != "") "**__[${rule.number}. ${rule.title}](${rule.link})__**\n${rule.description}"
            else "**__${rule.number}. ${rule.title}__**\n${rule.description}"
            inline = false
        }
    }
    footer {
        icon = guild.getIconUrl(Image.Format.PNG) ?: ""
        text = guild.name
    }
}
