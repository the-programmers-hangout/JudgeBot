package me.ddivad.judgebot

import dev.kord.common.kColor
import dev.kord.core.supplier.EntitySupplyStrategy
import dev.kord.gateway.Intent
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.services.BotStatsService
import me.ddivad.judgebot.services.PermissionLevel
import me.ddivad.judgebot.services.PermissionsService
import me.ddivad.judgebot.services.infractions.BanService
import me.ddivad.judgebot.services.infractions.MuteService
import me.ddivad.judgebot.services.requiredPermissionLevel
import me.jakejmattson.discordkt.api.dsl.bot
import me.jakejmattson.discordkt.api.extensions.addInlineField
import java.awt.Color

@PrivilegedIntent
suspend fun main(args: Array<String>) {
    val token = System.getenv("BOT_TOKEN") ?: null
    val defaultPrefix = System.getenv("DEFAULT_PREFIX") ?: "j!"

    require(token != null) { "Expected the bot token as an environment variable" }

    bot(token) {
        prefix {
            val configuration = discord.getInjectionObjects(Configuration::class)
            guild?.let { configuration[guild!!.id.value]?.prefix } ?: defaultPrefix
        }

        configure {
            allowMentionPrefix = true
            commandReaction = null
            theme = Color.MAGENTA
            entitySupplyStrategy = EntitySupplyStrategy.cacheWithCachingRestFallback
        }

        mentionEmbed {
            val botStats = it.discord.getInjectionObjects(BotStatsService::class)
            val channel = it.channel
            val self = channel.kord.getSelf()

            color = it.discord.configuration.theme?.kColor

            thumbnail {
                url = self.avatar.url
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
                    "Version:   2.2.2\n" +
                    "DiscordKt: ${versions.library}\n" +
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

        permissions {
            val permissionsService = discord.getInjectionObjects(PermissionsService::class)
            val permission = command.requiredPermissionLevel
            if (guild != null)
                permissionsService.hasClearance(guild!!, user, permission)
            else
                return@permissions command.requiredPermissionLevel == PermissionLevel.Everyone
        }

        onStart {
            val (muteService, banService) = this.getInjectionObjects(
                MuteService::class,
                BanService::class,
            )
            try {
                muteService.initGuilds()
                banService.initialiseBanTimers()
            } catch (ex: Exception) {
                println(ex.message)
            }
        }
    }
}
