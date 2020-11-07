package me.ddivad.judgebot.services

import com.gitlab.kordlib.core.any
import com.gitlab.kordlib.core.entity.Guild
import com.gitlab.kordlib.core.entity.Member
import com.gitlab.kordlib.core.entity.User
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
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
            requiredPermissionLevel == PermissionLevel.Everyone || user.id.value == configuration.ownerId
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

    private fun Member.isBotOwner() = id.value == configuration.ownerId
    private suspend fun Member.isGuildOwner() = isOwner()
    private suspend fun Member.isAdministrator() = roles.any { it.id.value == configuration[guild.id.longValue]?.adminRole }
    private suspend fun Member.isStaff() = roles.any { it.id.value == configuration[guild.id.longValue]?.staffRole }
    private suspend fun Member.isModerator() = roles.any { it.id.value == configuration[guild.id.longValue]?.moderatorRole }
}

var Command.requiredPermissionLevel: PermissionLevel
    get() = commandPermissions[this] ?: DEFAULT_REQUIRED_PERMISSION
    set(value) {
        commandPermissions[this] = value
    }