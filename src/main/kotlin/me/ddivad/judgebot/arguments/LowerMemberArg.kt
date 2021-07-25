package me.ddivad.judgebot.arguments

import dev.kord.core.entity.Member
import me.jakejmattson.discordkt.api.arguments.*
import me.jakejmattson.discordkt.api.dsl.CommandEvent
import me.jakejmattson.discordkt.api.extensions.toSnowflakeOrNull

open class LowerMemberArg(override val name: String = "LowerMemberArg") : ArgumentType<Member> {
    companion object : LowerMemberArg()

    override val description = "A Member with a lower rank"

    override suspend fun generateExamples(event: CommandEvent<*>) = mutableListOf("@User", "197780697866305536", "302134543639511050")

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Member> {
        val guild = event.guild ?: return Error("No guild found")

        val member = arg.toSnowflakeOrNull()?.let { guild.getMemberOrNull(it) } ?: return Error("Not found")
        val author = event.author.asMember(event.guild!!.id)

        return when {
            event.discord.permissions.isHigherLevel(event.discord, author, member) ->
                Error("You don't have the permission to use this command on the target user.")
            else -> Success(member)
        }
    }

    override fun formatData(data: Member) = "@${data.tag}"
}