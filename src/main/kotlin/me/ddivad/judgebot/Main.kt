package me.ddivad.judgebot

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.services.BotStatsService
import me.ddivad.judgebot.services.PermissionsService
import me.ddivad.judgebot.services.requiredPermissionLevel
import me.jakejmattson.discordkt.api.dsl.bot
import me.jakejmattson.discordkt.api.extensions.addInlineField
import java.awt.Color
import java.util.*

suspend fun main(args: Array<String>) {
    val token = args.firstOrNull()

    require(token != null) { "Expected the bot token as a command line argument!" }

    bot(token) {
        prefix {
            val configuration = discord.getInjectionObjects(Configuration::class)

            guild?.let { configuration[guild!!.id.longValue]?.prefix } ?: "<none>"
        }

        configure {
            allowMentionPrefix = true
            commandReaction = null
            theme = Color.MAGENTA
        }

        mentionEmbed {
            val botStats = it.discord.getInjectionObjects(BotStatsService::class)
            val channel = it.channel
            val self = channel.kord.getSelf()

            color = it.discord.configuration.theme

            thumbnail {
                url = self.avatar.url
            }

            field {
                name = self.tag
                value = "A bot for managing discord infractions in an intelligent and user-friendly way."
            }

            addInlineField("Prefix", it.prefix())
            addInlineField("Contributors", "ddivad#0001")

            val kotlinVersion = KotlinVersion.CURRENT
            val versions = it.discord.versions
            field {
                name = "Build Info"
                value = "```" +
                        "Version:   0.0.1\n" +
                        "DiscordKt: ${versions.library}\n" +
                        "Kotlin:    $kotlinVersion" +
                        "```"
            }

            field {
                name = "Uptime"
                value = botStats.uptime
            }
            field {
                name = "Ping"
                value = botStats.ping
            }
        }

        permissions {
            val permissionsService = discord.getInjectionObjects(PermissionsService::class)
            val permission = command.requiredPermissionLevel
            if (guild != null)
                permissionsService.hasClearance(guild!!, user, permission)
            else
                false
        }
    }
}