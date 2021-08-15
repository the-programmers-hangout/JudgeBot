package me.ddivad.judgebot.listeners

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.core.entity.PermissionOverwrite
import dev.kord.core.event.channel.TextChannelCreateEvent
import dev.kord.core.event.channel.thread.ThreadChannelCreateEvent
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.services.LoggingService
import me.jakejmattson.discordkt.api.dsl.listeners
import me.jakejmattson.discordkt.api.extensions.toSnowflake

@Suppress("unused")
fun onChannelCreated(configuration: Configuration, loggingService: LoggingService) = listeners {
    on<TextChannelCreateEvent> {
        val channel = this.channel
        val guild = channel.getGuild()
        val guildConfiguration = configuration[guild.id.value] ?: return@on
        val mutedRole = guild.getRole(guildConfiguration.mutedRole.toSnowflake())
        val deniedPermissions = channel.getPermissionOverwritesForRole(mutedRole.id)?.denied ?: Permissions()
        if (deniedPermissions.values.any { it in setOf(Permission.SendMessages, Permission.AddReactions, Permission.UsePublicThreads, Permission.UsePrivateThreads) }) {
            channel.addOverwrite(
                PermissionOverwrite.forRole(
                    mutedRole.id,
                    denied = deniedPermissions.plus(Permission.SendMessages).plus(Permission.AddReactions).plus(Permission.UsePublicThreads).plus(Permission.UsePrivateThreads)
                ),
                "Judgebot Overwrite"
            )
            loggingService.channelOverrideAdded(guild, channel)
        }
    }
}