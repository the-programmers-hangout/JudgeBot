package me.ddivad.judgebot.commands

import dev.kord.core.behavior.interaction.response.respond
import dev.kord.rest.builder.message.modify.embed
import me.ddivad.judgebot.arguments.LowerUserArg
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.dataclasses.Permissions
import me.ddivad.judgebot.embeds.createActivePunishmentsEmbed
import me.ddivad.judgebot.services.DatabaseService
import me.ddivad.judgebot.services.infractions.MuteService
import me.jakejmattson.discordkt.arguments.ChoiceArg
import me.jakejmattson.discordkt.arguments.IntegerArg
import me.jakejmattson.discordkt.arguments.MemberArg
import me.jakejmattson.discordkt.commands.commands
import me.jakejmattson.discordkt.extensions.TimeStamp
import java.time.Instant

@Suppress("Unused")
fun adminCommands(databaseService: DatabaseService, configuration: Configuration, muteService: MuteService) =
    commands("Admin", Permissions.ADMINISTRATOR) {
        slash(
            "removeInfraction",
            "Use this to delete (permanently) an infraction from a user.",
            Permissions.ADMINISTRATOR
        ) {
            execute(MemberArg("User", "Target User"), IntegerArg("ID", "Infraction ID")) {
                val user = databaseService.users.getOrCreateUser(args.first, guild)
                if (user.getGuildInfo(guild.id.toString()).infractions.isEmpty()) {
                    respond("User has no infractions.")
                    return@execute
                }
                databaseService.users.removeInfraction(guild, user, args.second)
                respond("Infraction removed.")
            }
        }

        slash("reset", "Reset a user's notes, infractions or whole record", Permissions.ADMINISTRATOR) {
            execute(
                LowerUserArg,
                ChoiceArg(
                    "choice",
                    "Part of the user record to reset",
                    "Infractions",
                    "Notes",
                    "Info",
                    "Point Decay",
                    "All"
                )
            ) {
                val (target, choice) = args
                val user = databaseService.users.getOrCreateUser(target, guild)
                when (choice) {
                    "Infractions" -> {
                        if (user.getGuildInfo(guild.id.toString()).infractions.isEmpty()) {
                            respond("User has no infractions.")
                            return@execute
                        }
                        databaseService.users.cleanseInfractions(guild, user)
                        respondPublic("Infractions reset for ${target.mention}.")
                    }
                    "Notes" -> {
                        if (user.getGuildInfo(guild.id.toString()).notes.isEmpty()) {
                            respond("User has no notes.")
                            return@execute
                        }
                        databaseService.users.cleanseNotes(guild, user)
                        respondPublic("Notes reset for ${target.mention}.")
                    }
                    "Point Decay" -> {
                        databaseService.users.updatePointDecayState(guild, user, false)
                        respondPublic("Point decay reset for ${target.mention}.")
                    }
                    "All" -> {
                        databaseService.users.resetUserRecord(guild, user)
                        respondPublic("User ${target.mention} reset.")
                    }
                }
            }
        }

        slash("activePunishments", "View active punishments for a guild.", Permissions.ADMINISTRATOR) {
            execute {
                val interactionResponse = interaction?.deferEphemeralResponse() ?: return@execute
                val punishments = databaseService.guilds.getActivePunishments(guild)
                if (punishments.isEmpty()) {
                    interactionResponse.respond { content = "No active punishments found." }
                    return@execute
                }
                interactionResponse.respond { embed { createActivePunishmentsEmbed(guild, punishments) } }
            }
        }

        slash("pointDecay", "Freeze point decay for a user", Permissions.ADMINISTRATOR) {
            execute(
                ChoiceArg("Option", "Freeze / Unfreeze point decay", "Freeze", "Unfreeze", "Thin Ice"),
                LowerUserArg
            ) {
                val (choice, user) = args
                var guildMember = databaseService.users.getOrCreateUser(user, guild)
                when (choice) {
                    "Freeze" -> {
                        databaseService.users.updatePointDecayState(guild, guildMember, true)
                        respondPublic("Point decay **frozen** for ${user.mention}")
                    }
                    "Unfreeze" -> {
                        databaseService.users.updatePointDecayState(guild, guildMember, false)
                        respondPublic("Point decay **unfrozen** for ${user.mention}")
                    }
                    else -> {
                        guildMember = databaseService.users.enableThinIceMode(guild, guildMember)
                        respondPublic(
                            "Point decay frozen and points set to 40 for ${user.mention}. Point decay will resume on ${
                                TimeStamp.at(
                                    Instant.ofEpochMilli(guildMember.getGuildInfo(guild.id.toString()).pointDecayTimer)
                                )
                            }"
                        )
                    }
                }
            }
        }
    }