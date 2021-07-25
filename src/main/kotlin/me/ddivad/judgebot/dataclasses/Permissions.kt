package me.ddivad.judgebot.dataclasses

import dev.kord.common.entity.Permission
import dev.kord.core.any
import me.jakejmattson.discordkt.api.dsl.PermissionContext
import me.jakejmattson.discordkt.api.dsl.PermissionSet

enum class Permissions : PermissionSet {
    BOT_OWNER {
        override suspend fun hasPermission(context: PermissionContext) =
            context.discord.getInjectionObjects<Configuration>().ownerId == context.user.id.asString
    },
    GUILD_OWNER {
        override suspend fun hasPermission(context: PermissionContext) =
            context.getMember()?.isOwner() ?: false

    },
    ADMINISTRATOR {
        override suspend fun hasPermission(context: PermissionContext): Boolean {
            val guild = context.guild ?: return false
            val member = context.user.asMember(guild.id)
            val configuration = context.discord.getInjectionObjects<Configuration>()
            return member.roles.any { configuration[guild.id.value]!!.adminRoles.contains(it.id.asString) } || member.getPermissions()
                .contains(
                    Permission.Administrator
                )
        }
    },
    STAFF {
        override suspend fun hasPermission(context: PermissionContext): Boolean {
            val guild = context.guild ?: return false
            val member = context.user.asMember(guild.id)
            val configuration = context.discord.getInjectionObjects<Configuration>()
            return member.roles.any { configuration[guild.id.value]!!.staffRoles.contains(it.id.asString) }
        }
    },
    MODERATOR {
        override suspend fun hasPermission(context: PermissionContext): Boolean {
            val guild = context.guild ?: return false
            val member = context.user.asMember(guild.id)
            val configuration = context.discord.getInjectionObjects<Configuration>()
            return member.roles.any { configuration[guild.id.value]!!.moderatorRoles.contains(it.id.asString) }
        }
    },
    NONE {
        override suspend fun hasPermission(context: PermissionContext) = true
    }
}