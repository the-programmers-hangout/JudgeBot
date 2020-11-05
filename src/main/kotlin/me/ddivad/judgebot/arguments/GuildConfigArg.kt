package me.ddivad.judgebot.arguments

<<<<<<< HEAD
=======
import me.ddivad.judgebot.dataclasses.GuildConfiguration
>>>>>>> 63cd7656c9035e2c078eb824e971c674ac10d7f3
import me.jakejmattson.discordkt.api.arguments.ArgumentResult
import me.jakejmattson.discordkt.api.arguments.ArgumentType
import me.jakejmattson.discordkt.api.arguments.Success
import me.jakejmattson.discordkt.api.arguments.Error
import me.jakejmattson.discordkt.api.dsl.CommandEvent

<<<<<<< HEAD
val validParameters = mutableListOf(
        "setMuteRole",
        "setPrefix",
        "setAdminRole",
        "setStaffRole",
        "setLogChannel",
        "setAlertChannel",
        "view",
        "list"
)

open class GuildConfigArg(override val name: String = "GuildConfig") : ArgumentType<String>() {
    override fun generateExamples(event: CommandEvent<*>): MutableList<String> = validParameters
=======
open class GuildConfigArg(override val name: String = "GuildConfig") : ArgumentType<String>() {
    override fun generateExamples(event: CommandEvent<*>): MutableList<String> = mutableListOf("setPrefix", "setStaffRole", "setAdminRole", "setMuteRole", "setLogChannel", "view")
>>>>>>> 63cd7656c9035e2c078eb824e971c674ac10d7f3

    companion object : GuildConfigArg()

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<String> {
<<<<<<< HEAD
        val parameters = validParameters.map { it.toLowerCase() }
        val parameter = arg.toLowerCase()
        return if (parameters.contains(parameter)) Success(parameter) else Error("$arg is not a valid configuration parameter")
=======
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
>>>>>>> 63cd7656c9035e2c078eb824e971c674ac10d7f3
    }
}