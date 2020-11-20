package me.ddivad.judgebot.conversations

import com.gitlab.kordlib.core.entity.Guild
import com.gitlab.kordlib.core.entity.Member
import me.ddivad.judgebot.dataclasses.*
import me.ddivad.judgebot.embeds.createHistoryEmbed
import me.ddivad.judgebot.embeds.createInfractionRuleEmbed
import me.ddivad.judgebot.services.DatabaseService
import me.ddivad.judgebot.services.infractions.InfractionService
import me.jakejmattson.discordkt.api.arguments.IntegerArg
import me.jakejmattson.discordkt.api.dsl.conversation

class InfractionConversation(private val databaseService: DatabaseService,
                             private val configuration: Configuration,
                             private val infractionService: InfractionService) {
    fun createInfractionConversation(guild: Guild, targetUser: Member, weight: Int, infractionReason: String, type: InfractionType) = conversation("cancel") {
        val guildConfiguration = configuration[guild.id.longValue] ?: return@conversation
        val user = databaseService.users.getOrCreateUser(targetUser, guild)
        val points = weight *
                if (type == InfractionType.Strike) guildConfiguration.infractionConfiguration.strikePoints
                else guildConfiguration.infractionConfiguration.warnPoints
        val rules = databaseService.guilds.getRules(guild)
        val ruleId = if (rules.isNotEmpty()) {
            respond { createInfractionRuleEmbed(guild, rules) }
            val rule = promptMessage(IntegerArg, "Enter `0` for no rule, or rule id to add a rule:")
            if (rule > 0) rule else null
        } else null
        val infraction = Infraction(this.user.id.value, infractionReason, type, points, ruleId)
        infractionService.infract(targetUser, guild, user, infraction)
        respondMenu { createHistoryEmbed(targetUser, user, guild, configuration, databaseService) }
    }
}