package me.ddivad.judgebot.util

import dev.kord.common.entity.ChannelType
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.entity.channel.thread.ThreadChannel
import me.jakejmattson.discordkt.api.extensions.jumpLink

suspend fun createFlagMessage(user: User, message: Message, channel: MessageChannel): String {
    val isThread = channel.type in setOf(ChannelType.PublicGuildThread, ChannelType.PrivateThread)

    return "**Message Flagged**" +
            "\n**User**: ${user.mention}" +
            (if (isThread)
                "\n**Thread**: ${channel.mention} (${(channel as? ThreadChannel)?.parent?.mention})"
            else
                "\n**Channel**: ${channel.mention}") +
            "\n**Author:** ${message.author?.mention}" +
            "\n**Message:** ${message.jumpLink()}"
}