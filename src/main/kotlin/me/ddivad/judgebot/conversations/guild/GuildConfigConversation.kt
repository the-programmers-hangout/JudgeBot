package me.ddivad.judgebot.conversations.guild

import dev.kord.core.entity.Guild
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.dataclasses.LoggingConfiguration
import me.ddivad.judgebot.services.infractions.MuteService
import me.jakejmattson.discordkt.arguments.ChannelArg
import me.jakejmattson.discordkt.arguments.EveryArg
import me.jakejmattson.discordkt.arguments.RoleArg
import me.jakejmattson.discordkt.conversations.conversation

class GuildSetupConversation(private val configuration: Configuration, private val muteService: MuteService) {
    fun createSetupConversation(guild: Guild) = conversation("cancel") {
        val prefix = prompt(EveryArg, "Bot prefix:")
        val adminRole = prompt(RoleArg, "Admin role:")
        val staffRole = prompt(RoleArg, "Staff role:")
        val moderatorRole = prompt(RoleArg, "Moderator role:")
        val logChannel = prompt(ChannelArg, "Log Channel:")
        val alertChannel = prompt(ChannelArg, "Alert Channel:")
        val mutedRole = prompt(RoleArg, "Muted role:")

        configuration.setup(
                guild,
                prefix,
                adminRole,
                staffRole,
                moderatorRole,
                mutedRole,
                LoggingConfiguration(alertChannel.id.toString(), logChannel.id.toString()),
        )
        muteService.initGuilds()
    }
}
