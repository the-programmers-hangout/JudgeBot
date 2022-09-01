package me.ddivad.judgebot

import dev.kord.common.annotation.KordPreview
import dev.kord.gateway.Intent
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.services.infractions.MuteService
import me.jakejmattson.discordkt.dsl.bot
import java.awt.Color

@KordPreview
@PrivilegedIntent
suspend fun main() {
    val token = System.getenv("BOT_TOKEN") ?: null
    val defaultPrefix = System.getenv("DEFAULT_PREFIX") ?: "j!"

    require(token != null) { "Expected the bot token as an environment variable" }

    bot(token) {
        val configuration = data("config/config.json") { Configuration() }

        prefix {
            guild?.let { configuration[guild!!.id]?.prefix } ?: defaultPrefix
        }

        configure {
            commandReaction = null
            theme = Color.MAGENTA
            intents = Intents(
                Intent.Guilds,
                Intent.GuildBans,
                Intent.GuildMembers,
                Intent.DirectMessages,
                Intent.GuildMessageReactions,
                Intent.DirectMessagesReactions
            )
        }
        onStart {
            val muteService = this.getInjectionObjects(MuteService::class)
            try {
                muteService.initGuilds()
            } catch (ex: Exception) {
                println(ex.message)
            }
        }
    }
}
