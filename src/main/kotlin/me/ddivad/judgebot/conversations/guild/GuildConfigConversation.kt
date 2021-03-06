package me.ddivad.judgebot.conversations.guild

import dev.kord.core.entity.Guild
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.dataclasses.LoggingConfiguration
import me.ddivad.judgebot.services.infractions.MuteService
import me.jakejmattson.discordkt.api.arguments.ChannelArg
import me.jakejmattson.discordkt.api.arguments.EveryArg
import me.jakejmattson.discordkt.api.arguments.RoleArg
import me.jakejmattson.discordkt.api.conversations.conversation

class GuildSetupConversation(private val configuration: Configuration, private val muteService: MuteService) {
    fun createSetupConversation(guild: Guild) = conversation("cancel") {
        val prefix = promptMessage(EveryArg, "Bot prefix:")
        val adminRole = promptMessage(RoleArg, "Admin role:")
        val staffRole = promptMessage(RoleArg, "Staff role:")
        val moderatorRole = promptMessage(RoleArg, "Moderator role:")
        val logChannel = promptMessage(ChannelArg, "Log Channel:")
        val alertChannel = promptMessage(ChannelArg, "Alert Channel:")
        val mutedRole = promptMessage(RoleArg, "Muted role:")

        configuration.setup(
                guild,
                prefix,
                adminRole,
                staffRole,
                moderatorRole,
                mutedRole,
                LoggingConfiguration(alertChannel.id.asString, logChannel.id.asString),
        )
        muteService.initGuilds()
    }
}
