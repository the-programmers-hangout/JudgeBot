package me.ddivad.judgebot

import dev.kord.common.annotation.KordPreview
import dev.kord.core.supplier.EntitySupplyStrategy
import dev.kord.gateway.Intent
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.dataclasses.Permissions
import me.ddivad.judgebot.services.*
import me.ddivad.judgebot.services.infractions.BanService
import me.ddivad.judgebot.services.infractions.MuteService
import me.jakejmattson.discordkt.dsl.bot
import me.jakejmattson.discordkt.extensions.addInlineField
import me.jakejmattson.discordkt.extensions.pfpUrl
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
            guild?.let { configuration[guild!!.id.value]?.prefix } ?: defaultPrefix
        }

        configure {
            allowMentionPrefix = true
            commandReaction = null
            theme = Color.MAGENTA
            entitySupplyStrategy = EntitySupplyStrategy.cacheWithRestFallback
            permissions = Permissions
            intents = Intents(
                Intent.Guilds,
                Intent.GuildBans,
                Intent.GuildMembers,
                Intent.DirectMessages,
                Intent.GuildMessageReactions,
                Intent.DirectMessagesReactions
            )
        }

        mentionEmbed {
            val botStats = it.discord.getInjectionObjects(BotStatsService::class)
            val channel = it.channel
            val self = channel.kord.getSelf()
            color = it.discord.configuration.theme
            thumbnail {
                url = self.pfpUrl
            }

            field {
                name = self.tag
                value = "A bot for managing discord infractions in an intelligent and user-friendly way."
            }

            addInlineField("Prefix", it.prefix())
            addInlineField("Ping", botStats.ping)
            addInlineField("Contributors", "[Link](https://github.com/the-programmers-hangout/JudgeBot/graphs/contributors)")

            val kotlinVersion = KotlinVersion.CURRENT
            val versions = it.discord.versions
            field {
                name = "Build Info"
                value = "```" +
                    "Version:   2.8.1\n" +
                    "DiscordKt: ${versions.library}\n" +
                    "Kord: ${versions.kord}\n" +
                    "Kotlin:    $kotlinVersion" +
                        "```"
            }
            field {
                name = "Uptime"
                value = botStats.uptime
            }
            field {
                name = "Source"
                value = "[Github](https://github.com/the-programmers-hangout/judgebot)"
            }
        }

        onStart {
            val (muteService, banService, cacheService) = this.getInjectionObjects(
                MuteService::class,
                BanService::class,
                CacheService::class
            )
            try {
                muteService.initGuilds()
                banService.initialiseBanTimers()
                cacheService.run()
            } catch (ex: Exception) {
                println(ex.message)
            }
        }
    }
}
