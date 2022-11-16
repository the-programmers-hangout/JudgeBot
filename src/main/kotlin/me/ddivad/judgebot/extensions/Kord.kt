package me.ddivad.judgebot.extensions

import dev.kord.core.entity.Member
import dev.kord.core.entity.User
import kotlinx.coroutines.flow.toList
import me.ddivad.judgebot.dataclasses.GuildConfiguration

suspend fun User.testDmStatus() {
    getDmChannel().createMessage("Infraction message incoming").delete()
}

suspend fun Member.getHighestRolePosition(): Int {
    if (isBot) return -1
    return roles.toList().maxByOrNull { it.rawPosition }?.rawPosition ?: -1
}

fun Member.hasStaffRoles(guildConfiguration: GuildConfiguration): Boolean {
    val staffRoleIds =
        ((guildConfiguration.adminRoles union guildConfiguration.staffRoles) union guildConfiguration.moderatorRoles)
    return staffRoleIds.any { roleIds.contains(it) }
}

fun Member.hasAdminRoles(guildConfiguration: GuildConfiguration): Boolean {
    return guildConfiguration.adminRoles.any { roleIds.contains(it) }
}
