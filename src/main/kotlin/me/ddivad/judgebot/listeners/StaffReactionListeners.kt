package me.ddivad.judgebot.listeners

import com.gitlab.kordlib.common.exception.RequestException
import com.gitlab.kordlib.core.event.message.ReactionAddEvent
import com.gitlab.kordlib.kordx.emoji.Emojis
import com.gitlab.kordlib.kordx.emoji.addReaction
import me.ddivad.judgebot.arguments.isHigherRankedThan
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.embeds.createMessageDeleteEmbed
import me.ddivad.judgebot.embeds.createSelfHistoryEmbed
import me.ddivad.judgebot.services.DatabaseService
import me.ddivad.judgebot.services.PermissionLevel
import me.ddivad.judgebot.services.PermissionsService
import me.ddivad.judgebot.services.infractions.MuteService
import me.jakejmattson.discordkt.api.dsl.listeners
import me.jakejmattson.discordkt.api.extensions.isSelf
import me.jakejmattson.discordkt.api.extensions.sendPrivateMessage

@Suppress("unused")
fun onStaffReactionAdd(muteService: MuteService,
                       databaseService: DatabaseService,
                       permissionsService: PermissionsService,
                       configuration: Configuration) = listeners {
    on<ReactionAddEvent> {
        val guild = guild?.asGuildOrNull() ?: return@on
        val guildConfiguration = configuration[guild.asGuild().id.longValue]
        if (!guildConfiguration?.reactions!!.enabled) return@on
        val member = user.asMemberOrNull(guild.id) ?: return@on
        val messageAuthor = message.asMessage().author?.asMemberOrNull(guild.id) ?: return@on
        val msg = message.asMessage()

        if (permissionsService.hasPermission(member, PermissionLevel.Moderator) && !member.isHigherRankedThan(permissionsService, messageAuthor)) {
            when (this.emoji.name) {
                guildConfiguration.reactions.gagReaction -> {
                    msg.deleteReaction(this.emoji)
                    muteService.gag(messageAuthor.asMember(guild.id))
                    member.sendPrivateMessage("${messageAuthor.mention} gagged.")
                }
                guildConfiguration.reactions.historyReaction -> {
                    msg.deleteReaction(this.emoji)
                    val target = databaseService.users.getOrCreateUser(messageAuthor, guild.asGuild())
                    member.sendPrivateMessage { createSelfHistoryEmbed(messageAuthor, target, guild.asGuild(), configuration) }
                }
                guildConfiguration.reactions.deleteMessageReaction -> {
                    msg.deleteReaction(this.emoji)
                    msg.delete()
                    try {
                        messageAuthor.sendPrivateMessage {
                            createMessageDeleteEmbed(guild, msg)
                        }
                    } catch (ex: RequestException) {
                        this.user.sendPrivateMessage("User ${messageAuthor.mention} has DM's disabled." +
                                " Message deleted without notification.")
                    }

                }
                Emojis.question.unicode -> {
                    if (this.user.isSelf() || msg.author != this.message.kord.getSelf()) return@on
                    msg.deleteReaction(this.emoji)
                    msg.addReaction(Emojis.whiteCheckMark)
                }
            }
        }
    }
}
