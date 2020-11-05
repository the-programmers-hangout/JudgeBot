package me.ddivad.judgebot.arguments

import com.gitlab.kordlib.core.entity.Guild
import me.ddivad.judgebot.dataclasses.Rule
import me.ddivad.judgebot.services.DatabaseService
import me.jakejmattson.discordkt.api.arguments.ArgumentResult
import me.jakejmattson.discordkt.api.arguments.ArgumentType
import me.jakejmattson.discordkt.api.arguments.Success
import me.jakejmattson.discordkt.api.arguments.Error
import me.jakejmattson.discordkt.api.dsl.CommandEvent

open class RuleArg(override val name : String = "Rule"): ArgumentType<Rule>() {
    override fun generateExamples(event: CommandEvent<*>): List<String> = mutableListOf("1","2","3")

    companion object : RuleArg()

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Rule> {
        val guild : Guild = event.guild ?: return Error("Rule arguments cannot be used outside of guilds")

        val databaseService: DatabaseService = event.discord.getInjectionObjects(DatabaseService::class)

        val rule: Rule? = databaseService.guilds.getRule(guild, arg.toInt())

        return if (rule == null) Error("Rule with id: $arg not found") else Success(rule)
    }
}