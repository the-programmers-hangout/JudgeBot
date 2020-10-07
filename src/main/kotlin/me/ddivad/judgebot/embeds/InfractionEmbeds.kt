package me.ddivad.judgebot.embeds

import com.gitlab.kordlib.rest.builder.message.EmbedBuilder
import me.jakejmattson.discordkt.api.extensions.addField

fun EmbedBuilder.createInfractionEmbed() {
    addField("Infraction", "")
}