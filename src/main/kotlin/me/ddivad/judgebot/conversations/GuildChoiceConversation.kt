package me.ddivad.judgebot.conversations

import dev.kord.common.kColor
import dev.kord.core.entity.Guild
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.embeds.createSelfHistoryEmbed
import me.ddivad.judgebot.services.DatabaseService
import me.jakejmattson.discordkt.conversations.conversation
import me.jakejmattson.discordkt.extensions.pfpUrl
import java.awt.Color

fun guildChoiceConversation(
    guilds: List<Guild>,
    configuration: Configuration
) = conversation {
    val databaseService = discord.getInjectionObjects(DatabaseService::class)
    val guild = promptButton<Guild> {
        embed {
            color = Color.MAGENTA.kColor
            title = "Select Server"
            description = "Select the server you want to view the history for."
            thumbnail {
                url = discord.kord.getSelf().pfpUrl
            }
        }

        guilds.toList().chunked(5).forEach { row ->
            buttons {
                row.forEach { guild ->
                    button(guild.name, null, guild)
                }
            }
        }
    }

    val guildMember = databaseService.users.getOrCreateUser(user, guild)

    respond {
        createSelfHistoryEmbed(user, guildMember, guild, configuration)
    }
}
