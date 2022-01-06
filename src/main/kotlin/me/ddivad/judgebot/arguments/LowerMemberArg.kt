package me.ddivad.judgebot.arguments

import dev.kord.core.entity.Member
import me.jakejmattson.discordkt.arguments.*
import me.jakejmattson.discordkt.commands.CommandEvent
import me.jakejmattson.discordkt.extensions.isSelf
import me.jakejmattson.discordkt.extensions.toSnowflakeOrNull

open class LowerMemberArg(override val name: String = "LowerMemberArg") : Argument<Member> {
    companion object : LowerMemberArg()

    override val description = "A Member with a lower rank"

    override suspend fun generateExamples(event: CommandEvent<*>) = mutableListOf("@User", "197780697866305536", "302134543639511050")

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Member> {
        val guild = event.guild ?: return Error("No guild found")

        val member = arg.toSnowflakeOrNull()?.let { guild.getMemberOrNull(it) } ?: return Error("Not found")
        val author = event.author.asMember(event.guild!!.id)

        return when {
            event.discord.permissions.isHigherLevel(event.discord, member, author) || event.author.isSelf() ->
                Error("You don't have the permission to use this command on the target user.")
            event.author == member -> Error("You can't use this command on yourself!")
            else -> Success(member)
        }
    }

    override fun formatData(data: Member) = "@${data.tag}"
}