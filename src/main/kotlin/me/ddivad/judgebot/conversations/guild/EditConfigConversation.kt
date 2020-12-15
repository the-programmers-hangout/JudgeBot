package me.ddivad.judgebot.conversations.guild

import com.gitlab.kordlib.core.entity.Guild
import com.gitlab.kordlib.core.entity.channel.TextChannel
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.embeds.createConfigEmbed
import me.ddivad.judgebot.embeds.createConfigOptionsEmbed
import me.jakejmattson.discordkt.api.arguments.*
import me.jakejmattson.discordkt.api.dsl.conversation
import me.jakejmattson.discordkt.api.extensions.toSnowflake

class EditConfigConversation(private val configuration: Configuration) {
    fun createEditConfigurationConversation(guild: Guild, parameter: String) = conversation("cancel") {
        val guildConfiguration = configuration[guild.id.longValue]!!
        when (parameter) {
            "addadminrole" -> {
                val role = promptMessage(RoleArg, "Enter Admin role:")
                guildConfiguration.adminRoles.add(role.id.value)
                respond("Added **${role.name}** to Admin roles.")
            }
            "addstaffrole" -> {
                val role = promptMessage(RoleArg, "Enter Staff role:")
                guildConfiguration.staffRoles.add(role.id.value)
                respond("Added **${role.name}** to Staff roles.")
            }
            "addmoderatorrole" -> {
                val role = promptMessage(RoleArg, "Enter Moderator role:")
                guildConfiguration.moderatorRoles.add(role.id.value)
                respond("Added **${role.name}** to moderator roles.")
            }
            "removeadminrole" -> {
                respond("Current roles are: ${guildConfiguration.adminRoles.map { "**${guild.getRoleOrNull(it.toSnowflake())?.name}**" }}")
                val role = promptUntil(
                        RoleArg,
                        "Enter role to remove:",
                        "Role not in Admin role list.",
                        isValid = {role -> guildConfiguration.adminRoles.contains(role.id.value) })
                guildConfiguration.adminRoles.removeIf {it == role.id.value}
                respond("Removed **${role.name}** from Admin roles.")
            }
            "removestaffrole" -> {
                respond("Current roles are: ${guildConfiguration.staffRoles.map { "**${guild.getRoleOrNull(it.toSnowflake())?.name}**" }}")
                val role = promptUntil(
                        RoleArg,
                        "Enter role to remove:",
                        "Role not in Staff role list.",
                        isValid = {role -> guildConfiguration.staffRoles.contains(role.id.value) })
                guildConfiguration.staffRoles.removeIf {it == role.id.value}
                respond("Removed **${role.name}** from Staff roles.")
            }
            "removemoderatorrole" -> {
                respond("Current roles are: ${ guildConfiguration.moderatorRoles.map { "**${guild.getRoleOrNull(it.toSnowflake())?.name}**" }}")
                val role = promptUntil(
                        RoleArg,
                        "Enter role to remove:",
                        "Role not in Moderator role list.",
                        isValid = {role -> guildConfiguration.moderatorRoles.contains(role.id.value) })
                guildConfiguration.moderatorRoles.removeIf {it == role.id.value}
                respond("Removed **${role.name}** from Moderator roles.")
            }
            "setmutedrole" -> {
                val mutedRole = promptMessage(RoleArg, "Enter Mute role:")
                guildConfiguration.mutedRole = mutedRole.id.value
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