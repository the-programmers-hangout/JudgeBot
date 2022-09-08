package me.ddivad.judgebot.arguments

import dev.kord.core.entity.interaction.GuildAutoCompleteInteraction
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.services.DatabaseService
import me.jakejmattson.discordkt.arguments.AnyArg

fun autoCompletingRuleArg(databaseService: DatabaseService) = AnyArg("Rule", "Rule for infraction").autocomplete {
    val guild = (interaction as GuildAutoCompleteInteraction).getGuild()
    databaseService.guilds.getRules(guild)
        .map { "${it.number} - ${it.title}" }
        .filter { it.contains(input, true) }
}

fun autoCompletingWeightArg(configuration: Configuration) = AnyArg("Weight", "Strike Weight").autocomplete {
    val guild = (interaction as GuildAutoCompleteInteraction).getGuild()
    val guildConfig = configuration[guild.id]
    1.rangeTo(guildConfig!!.infractionConfiguration.pointCeiling / 10).toList().map { it.toString() }
        .filter { it.contains(input, true) }
}.optional("1")
