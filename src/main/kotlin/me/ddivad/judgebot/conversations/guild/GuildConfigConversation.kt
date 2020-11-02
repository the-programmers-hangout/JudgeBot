package me.ddivad.judgebot.conversations.guild

import com.gitlab.kordlib.core.entity.Guild
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.dataclasses.LoggingConfiguration
import me.jakejmattson.discordkt.api.arguments.ChannelArg
import me.jakejmattson.discordkt.api.arguments.EveryArg
import me.jakejmattson.discordkt.api.arguments.RoleArg
import me.jakejmattson.discordkt.api.dsl.conversation

class GuildSetupConversation(private val configuration: Configuration) {
    fun createSetupConversation(guild: Guild) = conversation {
        val prefix = promptMessage(EveryArg, "Bot prefix:")
        val adminRole = promptMessage(RoleArg, "Admin role:")
        val staffRole = promptMessage(RoleArg, "Staff role:")
        val logChannel = promptMessage(ChannelArg, "Log Channel:")
        val alertChannel = promptMessage(ChannelArg, "Alert Channel:")
        val mutedRole = promptMessage(RoleArg, "Muted role:")

        configuration.setup(
                guild,
                prefix,
                adminRole,
                staffRole,
                mutedRole,
                LoggingConfiguration(logChannel.id.value, alertChannel.id.value),
        )
    }
}