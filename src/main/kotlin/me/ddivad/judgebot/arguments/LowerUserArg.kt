package me.ddivad.judgebot.arguments

import dev.kord.core.entity.User
import me.jakejmattson.discordkt.api.arguments.*
import me.jakejmattson.discordkt.api.dsl.CommandEvent
import me.jakejmattson.discordkt.api.extensions.toSnowflakeOrNull

open class LowerUserArg(override val name: String = "LowerUserArg") : ArgumentType<User> {
    companion object : LowerUserArg()

    override val description = "A user with a lower rank"

    override suspend fun generateExamples(event: CommandEvent<*>) = mutableListOf("@User", "197780697866305536", "302134543639511050")

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<User> {
        val guild = event.guild ?: return Error("No guild found")

        val user = arg.toSnowflakeOrNull()?.let { guild.kord.getUser(it) } ?: return Error("User Not Found")
        val member = guild.getMemberOrNull(user.id) ?: return Success(user)
        val author = event.author.asMember(event.guild!!.id)

        return when {
            event.discord.permissions.isHigherLevel(event.discord, member, author) ->
                Error("You don't have the permission to use this command on the target user.")
            else -> Success(member.asUser())
        }
    }
}