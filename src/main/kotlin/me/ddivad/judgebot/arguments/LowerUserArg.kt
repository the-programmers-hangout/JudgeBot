package me.ddivad.judgebot.arguments

import dev.kord.core.entity.User
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.extensions.getHighestRolePosition
import me.ddivad.judgebot.extensions.hasAdminRoles
import me.ddivad.judgebot.extensions.hasStaffRoles
import me.jakejmattson.discordkt.arguments.Error
import me.jakejmattson.discordkt.arguments.Result
import me.jakejmattson.discordkt.arguments.Success
import me.jakejmattson.discordkt.arguments.UserArgument
import me.jakejmattson.discordkt.commands.DiscordContext

open class LowerUserArg(
    override val name: String = "LowerUserArg",
    override val description: String = "A User with a lower rank",
    private val allowsBot: Boolean = false
) : UserArgument<User> {
    companion object : LowerUserArg()

    override suspend fun transform(input: User, context: DiscordContext): Result<User> {
        val configuration = context.discord.getInjectionObjects(Configuration::class)
        val guild = context.guild ?: return Error("No guild found")
        val guildConfiguration = configuration[guild.id] ?: return Error("Guild not configured")
        val targetMember = input.asMemberOrNull(guild.id) ?: return Success(input)
        val authorAsMember = context.author.asMemberOrNull(guild.id) ?: return Error("Member not found.")

        if (!allowsBot && targetMember.isBot)
            return Error("Cannot be a bot")

        return if (authorAsMember.id == targetMember.id) {
            Error("Cannot run command on yourself")
        } else if (authorAsMember.hasAdminRoles(guildConfiguration) && !targetMember.hasAdminRoles(guildConfiguration)) {
            Success(targetMember.asUser())
        } else if (targetMember.hasStaffRoles(guildConfiguration)) {
            Error("Cannot run command on staff members")
        } else if (authorAsMember.getHighestRolePosition() > targetMember.getHighestRolePosition()) {
            Success(targetMember.asUser())
        } else Error("Missing permissions to target this member")
    }

    override suspend fun generateExamples(context: DiscordContext): List<String> = listOf(context.author.mention)
}