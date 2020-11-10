package me.ddivad.judgebot.listeners

import com.gitlab.kordlib.core.event.message.ReactionAddEvent
import com.gitlab.kordlib.kordx.emoji.Emojis
import com.gitlab.kordlib.rest.route.Route
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.embeds.createMessageDeleteEmbed
import me.ddivad.judgebot.embeds.createSelfHistoryEmbed
import me.ddivad.judgebot.services.DatabaseService
import me.ddivad.judgebot.services.LoggingService
import me.ddivad.judgebot.services.PermissionLevel
import me.ddivad.judgebot.services.PermissionsService
import me.ddivad.judgebot.services.infractions.MuteService
import me.jakejmattson.discordkt.api.dsl.listeners
import me.jakejmattson.discordkt.api.extensions.isImagePost
import me.jakejmattson.discordkt.api.extensions.sendPrivateMessage

fun onStaffReactionAdd(muteService: MuteService,
                       databaseService: DatabaseService,
                       permissionsService: PermissionsService,
                       configuration: Configuration) = listeners {
    on<ReactionAddEvent> {
        val guild = guild?.asGuildOrNull() ?: return@on
        val guildConfiguration = configuration[guild.asGuild().id.longValue]
        if (!guildConfiguration?.reactions!!.enabled) return@on

        user.asMemberOrNull(guild.id)?.let {
            if (permissionsService.hasPermission(it, PermissionLevel.Moderator)) {
                val messageAuthor = message.asMessage().author ?: return@on

                when (this.emoji.name) {
                    guildConfiguration.reactions.gagReaction -> {
                        message.deleteReaction(this.emoji)
                        muteService.gag(messageAuthor.asMember(guild.id))
                        it.sendPrivateMessage("${messageAuthor.mention} gagged.")
                    }
                    guildConfiguration.reactions.historyReaction -> {
                        message.deleteReaction(this.emoji)
                        val target = databaseService.users.getOrCreateUser(messageAuthor, guild!!.asGuild())
                        it.sendPrivateMessage { createSelfHistoryEmbed(messageAuthor, target, guild!!.asGuild(), configuration) }
                    }
                    guildConfiguration.reactions.deleteMessageReaction -> {
                        message.deleteReaction(this.emoji)
                        messageAuthor.sendPrivateMessage {
                            createMessageDeleteEmbed(guild, messageAuthor, message.asMessage())
                        }
                        message.delete()
                    }
                }
            }
        }
    }
}
