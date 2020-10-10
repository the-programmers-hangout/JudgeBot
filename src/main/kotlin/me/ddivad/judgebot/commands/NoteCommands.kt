package me.ddivad.judgebot.commands

import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.services.DatabaseService
import me.ddivad.judgebot.services.PermissionLevel
import me.ddivad.judgebot.services.requiredPermissionLevel
import me.jakejmattson.discordkt.api.arguments.EveryArg
import me.jakejmattson.discordkt.api.arguments.IntegerArg
import me.jakejmattson.discordkt.api.arguments.MemberArg
import me.jakejmattson.discordkt.api.dsl.commands

fun noteCommands(databaseService: DatabaseService, configuration: Configuration) = commands("Notes") {
    command("note") {
        description = "Use this to add a note to a user."
        requiresGuild = true
        requiredPermissionLevel = PermissionLevel.Staff
        execute(MemberArg, EveryArg("Note Content")) {
            val (target, note) = args
            val user = databaseService.users.getOrCreateUser(target, guild!!.id.value)
            databaseService.users.addNote(guild!!, user, note, author.id.value)
            respond("Note added.")
        }
    }

    command("deleteNote") {
        description = "Use this to add a delete a note from a user."
        requiresGuild = true
        requiredPermissionLevel = PermissionLevel.Staff
        execute(MemberArg, IntegerArg) {
            val (target, noteId) = args
            val user = databaseService.users.getOrCreateUser(target, guild!!.id.value)
            if (user.getGuildInfo(guild!!.id.value)!!.notes.isEmpty()) return@execute respond("User has no notes.")
            databaseService.users.deleteNote(guild!!, user, noteId)
            respond("Note deleted.")
        }
    }

    command("cleansenotes") {
        description = "Use this to delete (permanently) as user's notes."
        requiresGuild = true
        requiredPermissionLevel = PermissionLevel.Administrator
        execute(MemberArg) {
            val user = databaseService.users.getOrCreateUser(args.first, guild!!.id.value)
            if (user.getGuildInfo(guild!!.id.value)!!.notes.isEmpty()) return@execute respond("User has no notes.")
            databaseService.users.cleanseNotes(guild!!, user)
            respond("Notes cleansed.")
        }
    }
}