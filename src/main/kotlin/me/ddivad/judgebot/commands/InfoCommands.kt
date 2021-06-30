package me.ddivad.judgebot.commands

import dev.kord.common.exception.RequestException
import dev.kord.x.emoji.Emojis
import dev.kord.x.emoji.addReaction
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
                respond("${target.mention} has DM's disabled. Info will not be sent.")
                return@execute
            }
            val user = databaseService.users.getOrCreateUser(target, guild)
            val information = Info(content, author.id.asString)
            databaseService.users.addInfo(guild, user, information)
            target.sendPrivateMessage {
                createInformationEmbed(guild, target, information)
            }
            respond("Info added and sent to ${target.mention}.")
        }
    }

    guildCommand("removeInfo") {
        description = "Remove an information message from a member record."
        requiredPermissionLevel = PermissionLevel.Staff
        execute(LowerMemberArg, IntegerArg("Info ID")) {
            val (target, id) = args
            val user = databaseService.users.getOrCreateUser(target, guild)
            if (user.getGuildInfo(guild.id.asString).info.isEmpty()) {
                respond("${target.mention} has no information records.")
                return@execute
            }
            databaseService.users.removeInfo(guild, user, id)
            respond("Information record removed from ${target.mention}.")
        }
    }
}
