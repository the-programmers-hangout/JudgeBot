package me.ddivad.judgebot.services

import dev.kord.core.any
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Member
import dev.kord.core.entity.User
import me.ddivad.judgebot.dataclasses.Configuration
import me.jakejmattson.discordkt.api.annotations.Service
import me.jakejmattson.discordkt.api.dsl.Command

enum class PermissionLevel {
    Everyone,
    Moderator,
    Staff,
    Administrator,
    GuildOwner,
    BotOwner
}

val DEFAULT_REQUIRED_PERMISSION = PermissionLevel.Everyone
val commandPermissions: MutableMap<Command, PermissionLevel> = mutableMapOf()

@Service
class PermissionsService(private val configuration: Configuration) {
    suspend fun hasClearance(guild: Guild?, user: User, requiredPermissionLevel: PermissionLevel): Boolean {
        val permissionLevel = guild?.getMember(user.id)?.let { getPermissionLevel(it) }
        return if (permissionLevel == null) {
            requiredPermissionLevel == PermissionLevel.Everyone || user.id.asString == configuration.ownerId
        } else {
            permissionLevel >= requiredPermissionLevel
        }
    }

    suspend fun hasPermission(member: Member, level: PermissionLevel) = getPermissionLevel(member) >= level
    suspend fun getPermissionRank(member: Member) = getPermissionLevel(member).ordinal

    private suspend fun getPermissionLevel(member: Member) =
            when {
                member.isBotOwner() -> PermissionLevel.BotOwner
                member.isGuildOwner() -> PermissionLevel.GuildOwner
                member.isAdministrator() -> PermissionLevel.Administrator
                member.isStaff() -> PermissionLevel.Staff
                member.isModerator() -> PermissionLevel.Moderator
                else -> PermissionLevel.Everyone
            }

    private fun Member.isBotOwner() = id.asString == configuration.ownerId
    private suspend fun Member.isGuildOwner() = isOwner()
    private suspend fun Member.isAdministrator() = roles.any { configuration[guild.id.value]!!.adminRoles.contains(it.id.asString) }
    private suspend fun Member.isStaff() = roles.any { configuration[guild.id.value]!!.staffRoles.contains(it.id.asString) }
    private suspend fun Member.isModerator() = roles.any { configuration[guild.id.value]!!.moderatorRoles.contains(it.id.asString) }

    suspend fun isCommandVisible(guild: Guild, user: User, command: Command) =
        hasClearance(guild, user, command.requiredPermissionLevel)
}

var Command.requiredPermissionLevel: PermissionLevel
    get() = commandPermissions[this] ?: DEFAULT_REQUIRED_PERMISSION
    set(value) {
        commandPermissions[this] = value
    }