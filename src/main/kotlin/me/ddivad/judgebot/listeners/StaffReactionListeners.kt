package me.ddivad.judgebot.listeners

import com.gitlab.kordlib.common.exception.RequestException
import com.gitlab.kordlib.core.event.message.ReactionAddEvent
import com.gitlab.kordlib.kordx.emoji.Emojis
import com.gitlab.kordlib.kordx.emoji.addReaction
import me.ddivad.judgebot.arguments.isHigherRankedThan
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.embeds.createMessageDeleteEmbed
import me.ddivad.judgebot.embeds.createCondensedHistoryEmbed
import me.ddivad.judgebot.services.DatabaseService
import me.ddivad.judgebot.services.LoggingService
import me.ddivad.judgebot.services.PermissionLevel
import me.ddivad.judgebot.services.PermissionsService
import me.ddivad.judgebot.services.infractions.MuteService
import me.ddivad.judgebot.services.infractions.RoleState
import me.jakejmattson.discordkt.api.dsl.listeners
import me.jakejmattson.discordkt.api.extensions.isSelf
import me.jakejmattson.discordkt.api.extensions.sendPrivateMessage

@Suppress("unused")
fun onStaffReactionAdd(
    muteService: MuteService,
    databaseService: DatabaseService,
    permissionsService: PermissionsService,
    loggingService: LoggingService,
    configuration: Configuration
) = listeners {
    on<ReactionAddEvent> {
        val guild = guild?.asGuildOrNull() ?: return@on
        val guildConfiguration = configuration[guild.asGuild().id.longValue]
        if (!guildConfiguration?.reactions!!.enabled) return@on
        val staffMember = user.asMemberOrNull(guild.id) ?: return@on
        val messageAuthor = message.asMessage().author?.asMemberOrNull(guild.id) ?: return@on
        val msg = message.asMessage()

        if (permissionsService.hasPermission(staffMember, PermissionLevel.Moderator) && !staffMember.isHigherRankedThan(
                permissionsService,
                messageAuthor
            )
        ) {
            when (this.emoji.name) {
                guildConfiguration.reactions.gagReaction -> {
                    loggingService.staffReactionUsed(guild, staffMember, messageAuthor, this.emoji)
                    msg.deleteReaction(this.emoji)
                    if (muteService.checkRoleState(guild, messageAuthor) == RoleState.Tracked) {
                        staffMember.sendPrivateMessage("${messageAuthor.mention} is already muted.")
                        return@on
                    }
                    muteService.gag(guild, messageAuthor, staffMember)
                    staffMember.sendPrivateMessage("${messageAuthor.mention} gagged.")
                }
                guildConfiguration.reactions.historyReaction -> {
                    loggingService.staffReactionUsed(guild, staffMember, messageAuthor, this.emoji)
                    msg.deleteReaction(this.emoji)
                    staffMember.sendPrivateMessage {
                        createCondensedHistoryEmbed(
                            messageAuthor,
                            databaseService.users.getOrCreateUser(messageAuthor, guild.asGuild()),
                            guild.asGuild(),
                            configuration
                        )
                    }
                }
                guildConfiguration.reactions.deleteMessageReaction -> {
                    loggingService.staffReactionUsed(guild, staffMember, messageAuthor, this.emoji)
                    msg.deleteReaction(this.emoji)
                    msg.delete()
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
