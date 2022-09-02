package me.ddivad.judgebot.listeners

import dev.kord.common.exception.RequestException
import dev.kord.core.event.message.ReactionAddEvent
import dev.kord.x.emoji.Emojis
import dev.kord.x.emoji.addReaction
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.embeds.createMessageDeleteEmbed
import me.ddivad.judgebot.extensions.getHighestRolePosition
import me.ddivad.judgebot.extensions.hasStaffRoles
import me.ddivad.judgebot.services.DatabaseService
import me.ddivad.judgebot.services.LoggingService
import me.ddivad.judgebot.services.infractions.MuteService
import me.ddivad.judgebot.services.infractions.MuteState
import me.jakejmattson.discordkt.dsl.listeners
import me.jakejmattson.discordkt.extensions.isSelf
import me.jakejmattson.discordkt.extensions.jumpLink
import me.jakejmattson.discordkt.extensions.sendPrivateMessage

@Suppress("unused")
fun onStaffReactionAdd(
    muteService: MuteService,
    databaseService: DatabaseService,
    loggingService: LoggingService,
    configuration: Configuration
) = listeners {
    on<ReactionAddEvent> {
        val guild = guild?.asGuildOrNull() ?: return@on
        val guildConfiguration = configuration[guild.asGuild().id]
        if (!guildConfiguration?.reactions!!.enabled) return@on
        val reactionUser = user.asMemberOrNull(guild.id) ?: return@on
        val msg = message.asMessage()
        val messageAuthor = msg.author?.asMemberOrNull(guild.id) ?: return@on

        if (reactionUser.hasStaffRoles(guildConfiguration) && reactionUser.getHighestRolePosition() > messageAuthor.getHighestRolePosition()) {
            when (this.emoji.name) {
                guildConfiguration.reactions.gagReaction -> {
                    msg.deleteReaction(this.emoji)
                    if (muteService.checkMuteState(guild, messageAuthor) == MuteState.Tracked) {
                        reactionUser.sendPrivateMessage("${messageAuthor.mention} is already muted.")
                        return@on
                    }
                    muteService.gag(guild, messageAuthor, reactionUser)
                    reactionUser.sendPrivateMessage("${messageAuthor.mention} gagged.")
                    loggingService.staffReactionUsed(guild, reactionUser, messageAuthor, this.emoji)
                }
                guildConfiguration.reactions.deleteMessageReaction -> {
                    message.delete()
                    databaseService.users.addMessageDelete(
                        guild,
                        databaseService.users.getOrCreateUser(messageAuthor, guild.asGuild()),
                        true
                    )
                    try {
                        messageAuthor.sendPrivateMessage {
                            createMessageDeleteEmbed(guild, msg)
                        }
                    } catch (ex: RequestException) {
                        reactionUser.sendPrivateMessage(
                            "User ${messageAuthor.mention} has DM's disabled." +
                                    " Message deleted without notification."
                        )
                    }
                    val deleteLogMessage =
                        loggingService.deleteReactionUsed(guild, reactionUser, messageAuthor, this.emoji, msg)
                    databaseService.messageDeletes.createMessageDeleteRecord(
                        guildId.toString(),
                        messageAuthor,
                        deleteLogMessage.first()?.jumpLink()
                    )
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
