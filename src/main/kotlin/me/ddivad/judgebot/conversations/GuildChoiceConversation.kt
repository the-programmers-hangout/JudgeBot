package me.ddivad.judgebot.conversations

import dev.kord.common.kColor
import dev.kord.core.entity.Guild
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.embeds.createSelfHistoryEmbed
import me.ddivad.judgebot.services.DatabaseService
import me.jakejmattson.discordkt.api.arguments.IntegerRangeArg
import me.jakejmattson.discordkt.api.dsl.conversation
import java.awt.Color

fun guildChoiceConversation(
    guilds: List<Guild>,
    configuration: Configuration
) = conversation {
    val databaseService = discord.getInjectionObjects(DatabaseService::class)
    val guildIndex = promptEmbed(IntegerRangeArg(1, guilds.size)) {
        title = "Select Server"
        description = "Respond with the server you want to view your history for."
        thumbnail {
            url = discord.kord.getSelf().avatar.url
        }
        color = Color.MAGENTA.kColor
        guilds.toList().forEachIndexed { index, guild ->
            field {
                name = "${index + 1}) ${guild.name}"
            }
        }
    } - 1

    val guild = guilds[guildIndex]
    val guildMember = databaseService.users.getOrCreateUser(user, guild)

    respond {
        createSelfHistoryEmbed(user, guildMember, guild, configuration)
    }
}
