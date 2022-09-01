package me.ddivad.judgebot.dataclasses

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions

@Suppress("unused")
object Permissions {
    val GUILD_OWNER = Permissions(Permission.ManageGuild)
    val ADMINISTRATOR = Permissions(Permission.Administrator)
    val STAFF = Permissions(Permission.BanMembers)
    val MODERATOR = Permissions(Permission.ManageMessages)
    val EVERYONE = Permissions(Permission.UseApplicationCommands)
}