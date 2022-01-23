package me.ddivad.judgebot.dataclasses

import me.jakejmattson.discordkt.dsl.Permission
import me.jakejmattson.discordkt.dsl.PermissionSet
import me.jakejmattson.discordkt.dsl.permission
import me.jakejmattson.discordkt.extensions.toSnowflake

@Suppress("unused")
object Permissions: PermissionSet{
    val BOT_OWNER = permission("Bot Owner") { users(discord.getInjectionObjects<Configuration>().ownerId.toSnowflake()) }
    val GUILD_OWNER = permission("Guild Owner") { guild?.let { users(it.ownerId) } }
    val ADMINISTRATOR = permission("Admin") { discord.getInjectionObjects<Configuration>()[guild!!.id.value]?.adminRoles?.let {
        roles(
            it.map { role -> role.toSnowflake() })
    } }
    val STAFF = permission("Staff") { discord.getInjectionObjects<Configuration>()[guild!!.id.value]?.staffRoles?.let {
        roles(
            it.map { role -> role.toSnowflake() })
    } }
    val MODERATOR = permission("Moderator") { discord.getInjectionObjects<Configuration>()[guild!!.id.value]?.moderatorRoles?.let {
        roles(
            it.map { role -> role.toSnowflake() })
    } }
    val NONE = permission("None") { guild?.let { roles(it.everyoneRole.id) } }

    override val hierarchy: List<Permission> = listOf(NONE, MODERATOR, STAFF, ADMINISTRATOR, GUILD_OWNER, BOT_OWNER)
    override val commandDefault: Permission = NONE
}