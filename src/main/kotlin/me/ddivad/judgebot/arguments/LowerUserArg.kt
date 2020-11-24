package me.ddivad.judgebot.arguments

import com.gitlab.kordlib.core.entity.User
import me.ddivad.judgebot.services.PermissionsService
import me.jakejmattson.discordkt.api.arguments.ArgumentResult
import me.jakejmattson.discordkt.api.arguments.ArgumentType
import me.jakejmattson.discordkt.api.dsl.CommandEvent
import me.jakejmattson.discordkt.api.arguments.Success
import me.jakejmattson.discordkt.api.arguments.Error
import me.jakejmattson.discordkt.api.extensions.toSnowflakeOrNull

open class LowerUserArg(override val name: String = "LowerUserArg") : ArgumentType<User>() {
    companion object : LowerUserArg()

    override fun generateExamples(event: CommandEvent<*>) = mutableListOf("@User", "197780697866305536", "302134543639511050")

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<User> {
        val permissionsService = event.discord.getInjectionObjects(PermissionsService::class)
        val guild = event.guild ?: return Error("No guild found")

        val user = arg.toSnowflakeOrNull()?.let { guild.kord.getUser(it) } ?: return Error("User Not Found")
        val member = guild.getMemberOrNull(user.id) ?: return Success(user)

        return when {
            event.author.asMember(event.guild!!.id).isHigherRankedThan(permissionsService, member) ->
                Error("You don't have the permission to use this command on the target user")
            else -> Success(member.asUser())
        }
    }
}