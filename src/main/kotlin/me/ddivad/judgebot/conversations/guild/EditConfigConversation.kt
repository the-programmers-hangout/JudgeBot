package me.ddivad.judgebot.conversations.guild

import com.gitlab.kordlib.core.entity.Guild
import com.gitlab.kordlib.core.entity.channel.TextChannel
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.embeds.createConfigEmbed
import me.jakejmattson.discordkt.api.arguments.ChannelArg
import me.jakejmattson.discordkt.api.arguments.EveryArg
import me.jakejmattson.discordkt.api.arguments.RoleArg
import me.jakejmattson.discordkt.api.dsl.conversation

class EditConfigConversation(private val configuration: Configuration) {
    fun createEditConfigurationConversation(guild: Guild, parameter: String) = conversation {
        val guildConfiguration = configuration[guild.id.longValue]!!
        when (parameter) {
            "setstaffrole" -> {
                val staffRole = promptMessage(RoleArg, "Enter Staff role:")
                guildConfiguration.staffRole = staffRole.id.value
                respond("Staff role set to **${staffRole.name}**.")
            }
            "setadminrole" -> {
                val adminRole = promptMessage(RoleArg, "Enter Admin role:")
                guildConfiguration.adminRole = adminRole.id.value
                respond("Admin role set to **${adminRole.name}**.")
            }
            "setmutedrole" -> {
                val mutedRole = promptMessage(RoleArg, "Enter Mute role:")
                guildConfiguration.adminRole = mutedRole.id.value
                respond("Muted role set to **${mutedRole.name}**.")
            }
            "setlogchannel" -> {
                val logChannel = promptMessage(ChannelArg<TextChannel>(), "Enter Logging channel:")
                guildConfiguration.loggingConfiguration.loggingChannel = logChannel.id.value
                respond("Log channel set to ${logChannel.mention}")
            }
            "setalertchannel" -> {
                val alertChannel = promptMessage(ChannelArg<TextChannel>(), "Enter Logging channel:")
                guildConfiguration.loggingConfiguration.alertChannel = alertChannel.id.value
                respond("Alert channel set to ${alertChannel.mention}")
            }
            "setprefix" -> {
                val prefix = promptMessage(EveryArg, "Enter Prefix:")
                guildConfiguration.prefix = prefix
                respond("Prefix set to **${prefix}**")
            }
            "view", "list" -> {
                respond {
                    createConfigEmbed(guildConfiguration, guild)
                }
            }
            else -> {
                respond("Configuration value not supported.")
                return@conversation
            }
        }
        configuration.save()
    }
}