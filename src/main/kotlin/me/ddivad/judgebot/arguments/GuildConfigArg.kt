package me.ddivad.judgebot.arguments

import me.jakejmattson.discordkt.api.arguments.*
import me.jakejmattson.discordkt.api.dsl.CommandEvent

val validConfigParameters = mutableListOf(
    "setPrefix",
    "addAdminRole",
    "addStaffRole",
    "addModeratorRole",
    "removeAdminRole",
    "removeStaffRole",
    "removeModeratorRole",
    "setMuteRole",
    "setLogChannel",
    "setAlertChannel",
    "setGagReaction",
    "setHistoryReaction",
    "setDeleteMessageReaction",
    "setFlagMessageReaction",
    "enableReactions",
    "view",
    "options"
)

open class GuildConfigArg(override val name: String = "GuildConfig") : ArgumentType<String> {
    override val description = "A Guild configuration"

    companion object : GuildConfigArg()

    override suspend fun generateExamples(event: CommandEvent<*>): MutableList<String> = validConfigParameters

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<String> {
        val parameters = validConfigParameters.map { it.toLowerCase() }
        val parameter = arg.toLowerCase()
        return if (parameters.contains(parameter)) Success(parameter) else Error("$arg is not a valid configuration parameter")
    }
}