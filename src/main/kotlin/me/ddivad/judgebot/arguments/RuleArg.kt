package me.ddivad.judgebot.arguments

import dev.kord.core.entity.Guild
import me.ddivad.judgebot.dataclasses.Rule
import me.ddivad.judgebot.services.DatabaseService
import me.jakejmattson.discordkt.api.arguments.*
import me.jakejmattson.discordkt.api.commands.CommandEvent

open class RuleArg(override val name: String = "Rule") : Argument<Rule> {
    override val description = "A rule number"

    override suspend fun generateExamples(event: CommandEvent<*>): List<String> = mutableListOf("1", "2", "3")

    companion object : RuleArg()

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Rule> {
        val guild: Guild = event.guild ?: return Error("Rule arguments cannot be used outside of guilds")

        val databaseService: DatabaseService = event.discord.getInjectionObjects(DatabaseService::class)

        val rule: Rule? = databaseService.guilds.getRule(guild, arg.toInt())

        return if (rule == null) Error("Rule with id: $arg not found") else Success(rule)
    }
}