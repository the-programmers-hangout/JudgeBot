package me.ddivad.judgebot.conversations

import dev.kord.common.kColor
import dev.kord.core.entity.Guild
import dev.kord.core.entity.User
import dev.kord.rest.Image
import dev.kord.x.emoji.Emojis
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.embeds.createHistoryEmbed
import me.ddivad.judgebot.services.DatabaseService
import me.jakejmattson.discordkt.api.conversations.conversation
import me.jakejmattson.discordkt.api.extensions.toSnowflake
import java.awt.Color

class ResetUserConversation(private val databaseService: DatabaseService, private val configuration: Configuration) {
    fun createResetConversation(guild: Guild, target: User) = conversation("cancel") {
        val user = databaseService.users.getOrCreateUser(target, guild)
        val guildMember = databaseService.users.resetUserRecord(guild, user)
        var response = "Reset ${target.mention}"
        val linkedAccounts = user.getLinkedAccounts(guild)
        if (linkedAccounts.isNotEmpty()) {
            val linkedUsers = linkedAccounts.map { guild.kord.getUser(it.toSnowflake()) }
            val resetLinked = promptButton<Boolean> {
                embed {
                    title = "Reset linked accounts"
                    color = Color.MAGENTA.kColor
                    thumbnail {
                        url = target.asUser().avatar.url
                    }
                    description = """
                        ${target.mention} has linked accounts ${linkedUsers.joinToString { "${it?.mention}" }}
                        
                        Reset linked accounts too? (${Emojis.whiteCheckMark.unicode} / ${Emojis.x.unicode})
                          """.trimIndent()
                    footer {
                        icon = guild.getIconUrl(Image.Format.PNG) ?: ""
                        text = guild.name
                    }
                }
                buttons {
                    button("Yes", Emojis.whiteCheckMark, true)
                    button("No", Emojis.x, false)
                }
            }
            if (resetLinked) {
                linkedUsers.forEach {
                    val altRecord = it?.let { record -> databaseService.users.getOrCreateUser(record, guild) }
                    if (altRecord != null) {
                        databaseService.users.resetUserRecord(guild, altRecord)
                        response += ", ${it.mention}"
                    }
                }
            }
        }
        respond(response)
        respondMenu { createHistoryEmbed(target, guildMember, guild, configuration, databaseService) }
    }
}