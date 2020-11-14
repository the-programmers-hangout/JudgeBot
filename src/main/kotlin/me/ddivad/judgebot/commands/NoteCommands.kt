package me.ddivad.judgebot.commands

import me.ddivad.judgebot.arguments.LowerMemberArg
import me.ddivad.judgebot.services.DatabaseService
import me.ddivad.judgebot.services.PermissionLevel
import me.ddivad.judgebot.services.requiredPermissionLevel
import me.jakejmattson.discordkt.api.arguments.EveryArg
import me.jakejmattson.discordkt.api.arguments.IntegerArg
import me.jakejmattson.discordkt.api.arguments.UserArg
import me.jakejmattson.discordkt.api.dsl.commands

@Suppress("unused")
fun noteCommands(databaseService: DatabaseService) = commands("Notes") {
    guildCommand("note") {
        description = "Use this to add a note to a user."
        requiredPermissionLevel = PermissionLevel.Moderator
        execute(UserArg, EveryArg("Note Content")) {
            val (target, note) = args
            val user = databaseService.users.getOrCreateUser(target, guild)
            databaseService.users.addNote(guild, user, note, author.id.value)
            respond("Note added.")
        }
    }

    guildCommand("deleteNote") {
        description = "Use this to add a delete a note from a user."
        requiredPermissionLevel = PermissionLevel.Staff
        execute(LowerMemberArg, IntegerArg) {
            val (target, noteId) = args
            val user = databaseService.users.getOrCreateUser(target, guild)
            if (user.getGuildInfo(guild.id.value).notes.isEmpty()) {
                respond("User has no notes.")
                return@execute
            }
            databaseService.users.deleteNote(guild, user, noteId)
            respond("Note deleted.")
        }
    }

    guildCommand("cleansenotes") {
        description = "Use this to delete (permanently) as user's notes."
        requiredPermissionLevel = PermissionLevel.Administrator
        execute(LowerMemberArg) {
            val user = databaseService.users.getOrCreateUser(args.first, guild)
            if (user.getGuildInfo(guild.id.value).notes.isEmpty()) {
                respond("User has no notes.")
                return@execute
            }
            databaseService.users.cleanseNotes(guild, user)
            respond("Notes cleansed.")
        }
    }
}