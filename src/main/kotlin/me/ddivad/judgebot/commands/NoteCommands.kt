package me.ddivad.judgebot.commands

import me.ddivad.judgebot.arguments.LowerUserArg
import me.ddivad.judgebot.dataclasses.Permissions
import me.ddivad.judgebot.services.DatabaseService
import me.jakejmattson.discordkt.arguments.EveryArg
import me.jakejmattson.discordkt.arguments.IntegerArg
import me.jakejmattson.discordkt.commands.subcommand

@Suppress("unused")
fun noteCommandsSub(databaseService: DatabaseService) = subcommand("Note", Permissions.MODERATOR) {
    sub("add", "Use this to add a note to a user.") {
        execute(LowerUserArg("User", "Target User"), EveryArg("Content", "Note content")) {
            val (target, note) = args
            val user = databaseService.users.getOrCreateUser(target, guild)
            databaseService.users.addNote(guild, user, note, author.id.toString())
            respondPublic("Note added to ${target.mention}: \n${note}")
        }
    }

    sub("edit", "Use this to edit a note.") {
        execute(
            LowerUserArg("User", "Target User"),
            IntegerArg("ID", "Note to edit"),
            EveryArg("Content", "Note content")
        ) {
            val (target, noteId, note) = args
            val user = databaseService.users.getOrCreateUser(target, guild)
            if (user.getGuildInfo(guild.id.toString()).notes.none { it.id == noteId }) {
                respond("User has no note with ID $noteId.")
                return@execute
            }
            databaseService.users.editNote(guild, user, noteId, note, author.id.toString())
            respondPublic("Note edited.")
        }
    }

    sub("delete", "Use this to delete a note from a user.") {
        execute(LowerUserArg("User", "Target User"), IntegerArg("ID", "Note ID")) {
            val (target, noteId) = args
            val user = databaseService.users.getOrCreateUser(target, guild)
            if (user.getGuildInfo(guild.id.toString()).notes.isEmpty()) {
                respond("User has no notes.")
                return@execute
            }
            databaseService.users.deleteNote(guild, user, noteId)
            respondPublic("Note deleted from ${target.mention}.")
        }
    }
}
