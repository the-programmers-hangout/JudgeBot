package me.ddivad.judgebot.listeners

import dev.kord.common.exception.RequestException
import dev.kord.core.event.message.ReactionAddEvent
import dev.kord.x.emoji.Emojis
import dev.kord.x.emoji.addReaction
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.dataclasses.Permissions
import me.ddivad.judgebot.embeds.createMessageDeleteEmbed
import me.ddivad.judgebot.embeds.createCondensedHistoryEmbed
import me.ddivad.judgebot.services.DatabaseService
import me.ddivad.judgebot.services.LoggingService
import me.ddivad.judgebot.services.infractions.MuteService
import me.ddivad.judgebot.services.infractions.RoleState
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
        val guildConfiguration = configuration[guild.asGuild().id.value]
        if (!guildConfiguration?.reactions!!.enabled) return@on
        val staffMember = user.asMemberOrNull(guild.id) ?: return@on
        val msg = message.asMessage()
        val messageAuthor = msg.author?.asMemberOrNull(guild.id) ?: return@on

        if ((discord.permissions.hasPermission(Permissions.MODERATOR, staffMember)
            && discord.permissions.getPermission(staffMember) > discord.permissions.getPermission(messageAuthor)
            || staffMember.id.toString() == configuration.ownerId)
        ) {
            when (this.emoji.name) {
                guildConfiguration.reactions.gagReaction -> {
                    msg.deleteReaction(this.emoji)
                    if (muteService.checkRoleState(guild, messageAuthor) == RoleState.Tracked) {
                        staffMember.sendPrivateMessage("${messageAuthor.mention} is already muted.")
                        return@on
                    }
                    muteService.gag(guild, messageAuthor, staffMember)
                    staffMember.sendPrivateMessage("${messageAuthor.mention} gagged.")
                    loggingService.staffReactionUsed(guild, staffMember, messageAuthor, this.emoji)
                }
                guildConfiguration.reactions.historyReaction -> {
                    msg.deleteReaction(this.emoji)
                    staffMember.sendPrivateMessage {
                        createCondensedHistoryEmbed(
                            messageAuthor,
                            databaseService.users.getOrCreateUser(messageAuthor, guild.asGuild()),
                            guild.asGuild(),
                            configuration
                        )
                    }
                    loggingService.staffReactionUsed(guild, staffMember, messageAuthor, this.emoji)
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
                        staffMember.sendPrivateMessage(
                            "User ${messageAuthor.mention} has DM's disabled." +
                                    " Message deleted without notification."
                        )
                    }
                    val deleteLogMessage = loggingService.deleteReactionUsed(guild, staffMember, messageAuthor, this.emoji, msg)
                    databaseService.messageDeletes.createMessageDeleteRecord(guildId.toString(), messageAuthor, deleteLogMessage.first()?.jumpLink())
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
