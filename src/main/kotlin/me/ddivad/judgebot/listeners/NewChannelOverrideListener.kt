package me.ddivad.judgebot.listeners

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.core.entity.PermissionOverwrite
import dev.kord.core.event.channel.TextChannelCreateEvent
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
        if (!deniedPermissions.contains(Permission.SendMessages) || !deniedPermissions.contains(Permission.AddReactions)) {
            channel.addOverwrite(
                PermissionOverwrite.forRole(
                    mutedRole.id,
                    denied = deniedPermissions.plus(Permission.SendMessages).plus(Permission.AddReactions)
                )
            )
            loggingService.channelOverrideAdded(guild, channel)
        }
    }
}