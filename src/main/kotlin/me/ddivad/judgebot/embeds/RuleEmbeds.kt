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
    if(rule.link !== "") {
        addField("", "[View this on our website](${rule.link})")
    }
    color = Color.MAGENTA
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