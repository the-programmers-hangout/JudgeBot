package me.ddivad.starter.conversations

import com.gitlab.kordlib.core.entity.Guild
import me.ddivad.starter.dataclasses.Configuration
import me.ddivad.starter.dataclasses.LoggingConfiguration
import me.jakejmattson.discordkt.api.arguments.ChannelArg
import me.jakejmattson.discordkt.api.arguments.EveryArg
import me.jakejmattson.discordkt.api.arguments.RoleArg
import me.jakejmattson.discordkt.api.dsl.Conversation
import me.jakejmattson.discordkt.api.dsl.conversation

class ConfigurationConversation(private val configuration: Configuration): Conversation() {
    @Conversation.Start
    fun createConfigurationConversation(guild: Guild) = conversation {
        val prefix = promptMessage(EveryArg, "Bot prefix:")
        val adminRole = promptMessage(RoleArg, "Admin role:")
        val staffRole = promptMessage(RoleArg, "Staff role:")
        val logChannel = promptMessage(ChannelArg, "Log Channel:")
        val alertChannel = promptMessage(ChannelArg, "Alert Channel:")

        configuration.setup(guild, prefix, adminRole, staffRole, LoggingConfiguration(logChannel.id.value, alertChannel.id.value))
    }
}