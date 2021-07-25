package me.ddivad.judgebot.listeners

import dev.kord.common.exception.RequestException
import dev.kord.core.event.message.ReactionAddEvent
import dev.kord.x.emoji.Emojis
import dev.kord.x.emoji.addReaction
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.embeds.createMessageDeleteEmbed
import me.ddivad.judgebot.embeds.createCondensedHistoryEmbed
import me.ddivad.judgebot.services.DatabaseService
import me.ddivad.judgebot.services.LoggingService
import me.ddivad.judgebot.services.infractions.MuteService
import me.ddivad.judgebot.services.infractions.RoleState
import me.jakejmattson.discordkt.api.dsl.listeners
import me.jakejmattson.discordkt.api.extensions.isSelf
import me.jakejmattson.discordkt.api.extensions.sendPrivateMessage

@Suppress("unused")
fun onStaffReactionAdd(
    muteService: MuteService,
    databaseService: DatabaseService,
    loggingService: LoggingService,
    configuration: Configuration
) = listeners {
    on<ReactionAddEvent> {
        println("listener")
//        val guild = guild?.asGuildOrNull() ?: return@on
//        val guildConfiguration = configuration[guild.asGuild().id.value]
//        if (!guildConfiguration?.reactions!!.enabled) return@on
//        val staffMember = user.asMemberOrNull(guild.id) ?: return@on
//        val messageAuthor = message.asMessage().author?.asMemberOrNull(guild.id) ?: return@on
//        val msg = message.asMessage()
//        println(discord.permissions.isHigherLevel(discord, staffMember, messageAuthor))
//        if (discord.permissions.isHigherLevel(discord, staffMember, messageAuthor)) {
//            when (this.emoji.name) {
//                guildConfiguration.reactions.gagReaction -> {
//                    msg.deleteReaction(this.emoji)
//                    if (muteService.checkRoleState(guild, messageAuthor) == RoleState.Tracked) {
//                        staffMember.sendPrivateMessage("${messageAuthor.mention} is already muted.")
//                        return@on
//                    }
//                    muteService.gag(guild, messageAuthor, staffMember)
//                    staffMember.sendPrivateMessage("${messageAuthor.mention} gagged.")
//                    loggingService.staffReactionUsed(guild, staffMember, messageAuthor, this.emoji)
//                }
//                guildConfiguration.reactions.historyReaction -> {
//                    msg.deleteReaction(this.emoji)
//                    staffMember.sendPrivateMessage {
//                        createCondensedHistoryEmbed(
//                            messageAuthor,
//                            databaseService.users.getOrCreateUser(messageAuthor, guild.asGuild()),
//                            guild.asGuild(),
//                            configuration
//                        )
//                    }
//                    loggingService.staffReactionUsed(guild, staffMember, messageAuthor, this.emoji)
//                }
//                guildConfiguration.reactions.deleteMessageReaction -> {
//                    msg.deleteReaction(this.emoji)
//                    msg.delete()
//                    databaseService.users.addMessageDelete(
//                        guild,
//                        databaseService.users.getOrCreateUser(messageAuthor, guild.asGuild()),
//                        true
//                    )
//                    try {
//                        messageAuthor.sendPrivateMessage {
//                            createMessageDeleteEmbed(guild, msg)
//                        }
//                    } catch (ex: RequestException) {
//                        staffMember.sendPrivateMessage(
//                            "User ${messageAuthor.mention} has DM's disabled." +
//                                    " Message deleted without notification."
//                        )
//                    }
//                    loggingService.staffReactionUsed(guild, staffMember, messageAuthor, this.emoji)
//                }
//                Emojis.question.unicode -> {
//                    if (this.user.isSelf() || msg.author != this.message.kord.getSelf()) return@on
//                    msg.deleteReaction(this.emoji)
//                    msg.addReaction(Emojis.whiteCheckMark)
//                }
//            }
//        }
    }
}
