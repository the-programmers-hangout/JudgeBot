package me.ddivad.judgebot.preconditions

import dev.kord.core.entity.channel.TextChannel
import me.ddivad.judgebot.dataclasses.Configuration
import me.jakejmattson.discordkt.dsl.precondition
import me.jakejmattson.discordkt.extensions.idDescriptor
import mu.KotlinLogging

val logger = KotlinLogging.logger { }

@Suppress("unused")
fun commandLogger(configuration: Configuration) = precondition {
    command ?: return@precondition
    if (guild != null) {
        val guild = guild!!
        val channel = channel as TextChannel
        val message = "${author.idDescriptor()} Invoked `${command!!.names.first()}` in #${channel.name}."
        logger.info { "${guild.name} (${guild.id}): $message" }
    }

    return@precondition
}