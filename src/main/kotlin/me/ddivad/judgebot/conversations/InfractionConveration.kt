package me.ddivad.judgebot.conversations

import com.gitlab.kordlib.core.entity.Guild
import com.gitlab.kordlib.core.entity.Member
import me.ddivad.judgebot.arguments.RuleArg
import me.ddivad.judgebot.dataclasses.*
import me.ddivad.judgebot.embeds.createHistoryEmbed
import me.ddivad.judgebot.services.DatabaseService
import me.ddivad.judgebot.services.infractions.InfractionService
import me.jakejmattson.discordkt.api.arguments.BooleanArg
import me.jakejmattson.discordkt.api.dsl.Conversation
import me.jakejmattson.discordkt.api.dsl.conversation

class InfractionConveration(private val databaseService: DatabaseService, private val configuration: Configuration, private val infractionService: InfractionService): Conversation() {
    @Conversation.Start
    fun createInfractionConversation(guild: Guild, targetUser: Member, weight: Int, infractionReason: String) = conversation {
        val guildConfiguration = configuration[guild.id.longValue] ?: return@conversation
        val user = databaseService.users.getOrCreateUser(targetUser, guild.id.value)
        val points = weight * guildConfiguration.infractionConfiguration.strikePoints
        val addRule = promptMessage(BooleanArg("Add Rule", "y", "n"), "Add rule? (y/n)")
        val rule = if (addRule) promptMessage(RuleArg, "Enter rule number: ").number else null
        val infraction = Infraction(this.user.id.value, infractionReason, InfractionType.Strike, points, rule)

        infractionService.infract(targetUser, guild, user, infraction)
        respondMenu { createHistoryEmbed(targetUser, user, guild, configuration, true) }
    }
}