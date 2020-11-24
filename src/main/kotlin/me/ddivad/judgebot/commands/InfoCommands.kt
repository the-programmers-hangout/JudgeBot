package me.ddivad.judgebot.commands

import com.gitlab.kordlib.common.exception.RequestException
import com.gitlab.kordlib.kordx.emoji.Emojis
import com.gitlab.kordlib.kordx.emoji.addReaction
import me.ddivad.judgebot.arguments.LowerMemberArg
import me.ddivad.judgebot.dataclasses.Info
import me.ddivad.judgebot.embeds.createInformationEmbed
import me.ddivad.judgebot.extensions.testDmStatus
import me.ddivad.judgebot.services.DatabaseService
import me.ddivad.judgebot.services.PermissionLevel
import me.ddivad.judgebot.services.requiredPermissionLevel
import me.jakejmattson.discordkt.api.arguments.EveryArg
import me.jakejmattson.discordkt.api.arguments.IntegerArg
import me.jakejmattson.discordkt.api.dsl.commands
import me.jakejmattson.discordkt.api.extensions.sendPrivateMessage

@Suppress("unused")
fun createInformationCommands(databaseService: DatabaseService) = commands("Information") {
    guildCommand("info") {
        description = "Send an information message to a guild member"
        requiredPermissionLevel = PermissionLevel.Moderator
        execute(LowerMemberArg, EveryArg("Info Content")) {
            val (target, content) = args
            try {
                target.testDmStatus()
                this.message.addReaction(Emojis.whiteCheckMark)
            } catch (ex: RequestException) {
                this.message.addReaction(Emojis.x)
                respond("Target user has DM's disabled. Info will not be sent.")
                return@execute
            }
            val user = databaseService.users.getOrCreateUser(target, guild)
            val information = Info(content, author.id.value)
            databaseService.users.addInfo(guild, user, information)
            target.sendPrivateMessage {
                createInformationEmbed(guild, target, information)
            }
            respond("Info added and sent to user.")
        }
    }

    guildCommand("removeInfo") {
        description = "Remove an information message from a member record."
        requiredPermissionLevel = PermissionLevel.Staff
        execute(LowerMemberArg, IntegerArg) {
            val (target, id) = args
            val user = databaseService.users.getOrCreateUser(target, guild)
            if (user.getGuildInfo(guild.id.value).info.isEmpty()) {
                respond("User has no information records.")
                return@execute
            }
            databaseService.users.removeInfo(guild, user, id)
            respond("Information record removed.")
        }
    }
}