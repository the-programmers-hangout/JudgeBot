package me.ddivad.judgebot

import me.ddivad.judgebot.dataclasses.Configuration
import me.jakejmattson.discordkt.dsl.precondition


@Suppress("unused")
fun prefixPrecondition(configuration: Configuration) = precondition {
    if (guild == null) return@precondition
    if (message == null) return@precondition
    if (author.isBot) return@precondition

    val guildConfig = configuration[guild!!.id] ?: return@precondition
    val content = message!!.content

    if (content.startsWith(guildConfig.prefix)) {
        fail("Text commands are deprecated. Please use the appropriate slash command.")
    }
}