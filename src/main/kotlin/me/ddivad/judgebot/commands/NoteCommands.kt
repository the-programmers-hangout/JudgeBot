package me.ddivad.judgebot.commands

import me.ddivad.judgebot.arguments.LowerMemberArg
import me.ddivad.judgebot.dataclasses.Permissions
import me.ddivad.judgebot.services.DatabaseService
import me.jakejmattson.discordkt.api.arguments.EveryArg
import me.jakejmattson.discordkt.api.arguments.IntegerArg
import me.jakejmattson.discordkt.api.arguments.UserArg
import me.jakejmattson.discordkt.api.dsl.commands

@Suppress("unused")
fun noteCommands(databaseService: DatabaseService) = commands("Note") {
    guildCommand("note") {
        description = "Use this to add a note to a user."
        requiredPermission = Permissions.MODERATOR
        execute(UserArg, EveryArg("Note Content")) {
            val (target, note) = args
            val user = databaseService.users.getOrCreateUser(target, guild)
            databaseService.users.addNote(guild, user, note, author.id.asString)
            respond("Note added to ${target.mention}.")
        }
    }

    guildCommand("editNote") {
        description = "Use this to edit a note."
        requiredPermission = Permissions.MODERATOR
        execute(UserArg, IntegerArg("Note to edit"), EveryArg("Note Content")) {
            val (target, noteId, note) = args
            val user = databaseService.users.getOrCreateUser(target, guild)
            if (user.getGuildInfo(guild.id.asString).notes.none{ it.id == noteId }) {
                respond("User has no note with ID $noteId.")
                return@execute
            }
            databaseService.users.editNote(guild, user, noteId, note, author.id.asString)
            respond("Note edited.")
        }
    }

    guildCommand("deleteNote") {
        description = "Use this to add a delete a note from a user."
        requiredPermission = Permissions.STAFF
        execute(LowerMemberArg, IntegerArg("Note ID")) {
            val (target, noteId) = args
            val user = databaseService.users.getOrCreateUser(target, guild)
            if (user.getGuildInfo(guild.id.asString).notes.isEmpty()) {
                respond("User has no notes.")
                return@execute
            }
            databaseService.users.deleteNote(guild, user, noteId)
            respond("Note deleted from ${target.mention}.")
        }
    }

    guildCommand("cleanseNotes") {
        description = "Use this to delete (permanently) as user's notes."
        requiredPermission = Permissions.ADMINISTRATOR
        execute(LowerMemberArg) {
            val target = args.first
            val user = databaseService.users.getOrCreateUser(target, guild)
            if (user.getGuildInfo(guild.id.asString).notes.isEmpty()) {
                respond("User has no notes.")
                return@execute
            }
            databaseService.users.cleanseNotes(guild, user)
            respond("Notes cleansed from ${target.mention}.")
        }
    }
}
