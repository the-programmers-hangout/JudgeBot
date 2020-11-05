package me.ddivad.judgebot.arguments

import me.ddivad.judgebot.dataclasses.GuildConfiguration
import me.jakejmattson.discordkt.api.arguments.ArgumentResult
import me.jakejmattson.discordkt.api.arguments.ArgumentType
import me.jakejmattson.discordkt.api.arguments.Success
import me.jakejmattson.discordkt.api.arguments.Error
import me.jakejmattson.discordkt.api.dsl.CommandEvent

open class GuildConfigArg(override val name: String = "GuildConfig") : ArgumentType<String>() {
    override fun generateExamples(event: CommandEvent<*>): MutableList<String> = mutableListOf("setPrefix", "setStaffRole", "setAdminRole", "setMuteRole", "setLogChannel", "view")

    companion object : GuildConfigArg()

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<String> {
        val validParameters = mutableListOf(
                "setMuteRole",
                "setPrefix",
                "setAdminRole",
                "setStaffRole",
                "setLogChannel",
                "setAlertChannel",
                "view"
        ).map { it.toLowerCase() }
        val parameter = arg.toLowerCase()
        return if (validParameters.contains(parameter)) Success(parameter) else Error("$arg is not a valid configuration parameter")
    }
}