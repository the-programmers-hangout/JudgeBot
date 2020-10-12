package me.ddivad.judgebot.listeners

import com.gitlab.kordlib.common.entity.Permission
import com.gitlab.kordlib.common.entity.Permissions
import com.gitlab.kordlib.core.entity.PermissionOverwrite
import com.gitlab.kordlib.core.event.channel.TextChannelCreateEvent
import com.gitlab.kordlib.core.event.guild.BanAddEvent
import kotlinx.coroutines.runBlocking
import me.ddivad.judgebot.dataclasses.Configuration
import me.jakejmattson.discordkt.api.dsl.listeners
import me.jakejmattson.discordkt.api.extensions.toSnowflake

fun onChannelCreated(configuration: Configuration) = listeners {
    on<TextChannelCreateEvent> {
        val channel = this.channel
        val guild = channel.getGuild()
        val mutedRole = guild.getRole(configuration[guild.id.longValue]!!.mutedRole.toSnowflake()!!)

        val deniedPermissions = channel.getPermissionOverwritesForRole(mutedRole.id)?.denied ?: Permissions()
        if (!deniedPermissions.contains(Permission.SendMessages) || !deniedPermissions.contains(Permission.AddReactions)) {
            channel.addOverwrite(
                    PermissionOverwrite.forRole(
                            mutedRole.id,
                            denied = deniedPermissions.plus(Permission.SendMessages).plus(Permission.AddReactions))
            )
        }
    }
}