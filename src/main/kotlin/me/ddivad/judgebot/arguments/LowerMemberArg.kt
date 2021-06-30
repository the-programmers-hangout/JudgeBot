package me.ddivad.judgebot.arguments

import dev.kord.core.entity.Member
import me.ddivad.judgebot.services.PermissionsService
import me.jakejmattson.discordkt.api.arguments.*
import me.jakejmattson.discordkt.api.dsl.CommandEvent
import me.jakejmattson.discordkt.api.extensions.toSnowflakeOrNull

open class LowerMemberArg(override val name: String = "LowerMemberArg") : ArgumentType<Member> {
    companion object : LowerMemberArg()

    override val description = "A Member with a lower rank"

    override suspend fun generateExamples(event: CommandEvent<*>) = mutableListOf("@User", "197780697866305536", "302134543639511050")

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Member> {
        val permissionsService = event.discord.getInjectionObjects(PermissionsService::class)
        val guild = event.guild ?: return Error("No guild found")

        val member = arg.toSnowflakeOrNull()?.let { guild.getMemberOrNull(it) } ?: return Error("Not found")

        return when {
            event.author.asMember(event.guild!!.id).isHigherRankedThan(permissionsService, member) ->
                Error("You don't have the permission to use this command on the target user.")
            else -> Success(member)
        }
    }

    override fun formatData(data: Member) = "@${data.tag}"
}

suspend fun Member.isHigherRankedThan(permissions: PermissionsService, targetMember: Member) =
    permissions.getPermissionRank(this) < permissions.getPermissionRank(targetMember)
