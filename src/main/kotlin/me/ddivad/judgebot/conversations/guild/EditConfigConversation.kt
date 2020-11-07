package me.ddivad.judgebot.conversations.guild

import com.gitlab.kordlib.core.entity.Guild
import com.gitlab.kordlib.core.entity.channel.TextChannel
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.embeds.createConfigEmbed
import me.ddivad.judgebot.embeds.createConfigOptionsEmbed
import me.jakejmattson.discordkt.api.arguments.*
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
            "setgagreaction" -> {
                val reaction = promptMessage(UnicodeEmojiArg, "Enter Reaction:")
                guildConfiguration.reactions.gagReaction = reaction.unicode
                respond("Reaction set to ${reaction.unicode}")
            }
            "sethistoryreaction" -> {
                val reaction = promptMessage(UnicodeEmojiArg, "Enter Reaction:")
                guildConfiguration.reactions.historyReaction = reaction.unicode
                respond("Reaction set to ${reaction.unicode}")
            }
            "setdeletemessagereaction" -> {
                val reaction = promptMessage(UnicodeEmojiArg, "Enter Reaction:")
                guildConfiguration.reactions.deleteMessageReaction = reaction.unicode
                respond("Reaction set to ${reaction.unicode}")
            }
            "setflagmessagereaction" -> {
                val reaction = promptMessage(UnicodeEmojiArg, "Enter Reaction:")
                guildConfiguration.reactions.flagMessageReaction = reaction.unicode
                respond("Reaction set to ${reaction.unicode}")
            }
            "enablereactions" -> {
                val enabled = promptMessage(BooleanArg("reaactions", "enable", "disable"), "enable / disable:")
                guildConfiguration.reactions.enabled = enabled
                respond("Reactions set to $enabled")
            }
            "view", "list" -> {
                respond {
                    createConfigEmbed(guildConfiguration, guild)
                }
            }
            "options" -> {
                respond {
                    createConfigOptionsEmbed(guildConfiguration, guild)
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