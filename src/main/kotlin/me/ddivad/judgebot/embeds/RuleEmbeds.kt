package me.ddivad.judgebot.embeds

import dev.kord.common.kColor
import dev.kord.core.entity.Guild
import dev.kord.rest.builder.message.EmbedBuilder
import me.ddivad.judgebot.dataclasses.Rule
import me.jakejmattson.discordkt.api.extensions.addField
import java.awt.Color
import dev.kord.rest.Image

fun EmbedBuilder.createRuleEmbed(guild: Guild, rule: Rule) {
    title = "__${rule.number}: ${rule.title}__"
    description = rule.description
    thumbnail {
        url = guild.getIconUrl(Image.Format.PNG) ?: ""
    }
    if (rule.link !== "") {
        addField("", "[View this on our website](${rule.link})")
    }
    color = Color.MAGENTA.kColor
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
    color = Color.MAGENTA.kColor

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
    color = Color.MAGENTA.kColor
    description = ""
    for (rule in rules) {
        description += "**[${rule.number}](${rule.link})**. ${rule.title}\n"
    }

    footer {
        icon = guild.getIconUrl(Image.Format.PNG) ?: ""
        text = guild.name
    }
    addField("","")
    addField("Rule Choice:", "Reply with **rule id**, or **0** for no rule.\nYou can also type **cancel** to stop this process.")
}

fun EmbedBuilder.createRulesEmbedDetailed(guild: Guild, rules: List<Rule>) {
    title = "**__Server Rules__**"
    thumbnail {
        url = guild.getIconUrl(Image.Format.PNG) ?: ""
    }
    color = Color.MAGENTA.kColor

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
