package me.ddivad.judgebot.services.database

import dev.kord.core.entity.Guild
import dev.kord.core.entity.User
import me.ddivad.judgebot.dataclasses.*
import me.ddivad.judgebot.services.LoggingService
import me.jakejmattson.discordkt.annotations.Service
import org.litote.kmongo.eq

@Service
class UserOperations(
    connection: ConnectionService,
    private val configuration: Configuration,
    private val joinLeaveService: JoinLeaveOperations,
    private val loggingService: LoggingService
) {
    private val userCollection = connection.db.getCollection<GuildMember>("Users")

    suspend fun getOrCreateUser(target: User, guild: Guild): GuildMember {
        val userRecord = userCollection.findOne(GuildMember::userId eq target.id.toString())
        return if (userRecord != null) {
            userRecord.ensureGuildDetailsPresent(guild.id.toString())
            userRecord.checkPointDecay(guild, configuration[guild.id]!!, loggingService)
            this.updateUser(userRecord)
            target.asMemberOrNull(guild.id)?.let {
                joinLeaveService.createJoinLeaveRecordIfNotRecorded(guild.id.toString(), it)
            }
            userRecord
        } else {
            val guildMember = GuildMember(target.id.toString())
            guildMember.guilds.add(GuildMemberDetails(guild.id.toString()))
            userCollection.insertOne(guildMember)
            guildMember
        }
    }

    suspend fun addNote(guild: Guild, user: GuildMember, note: String, moderator: String): GuildMember {
        user.addNote(note, moderator, guild)
        return this.updateUser(user)
    }

    suspend fun editNote(
        guild: Guild,
        user: GuildMember,
        noteId: Int,
        newContent: String,
        moderator: String
    ): GuildMember {
        user.editNote(guild, noteId, newContent, moderator)
        return this.updateUser(user)
    }

    suspend fun deleteNote(guild: Guild, user: GuildMember, noteId: Int): GuildMember {
        user.deleteNote(noteId, guild)
        return this.updateUser(user)
    }

    suspend fun addInfo(guild: Guild, user: GuildMember, information: Info): GuildMember {
        user.addInfo(information, guild)
        return this.updateUser(user)
    }

    suspend fun removeInfo(guild: Guild, user: GuildMember, noteId: Int): GuildMember {
        user.removeInfo(noteId, guild)
        return this.updateUser(user)
    }

    suspend fun addLinkedAccount(guild: Guild, user: GuildMember, userId: String): GuildMember {
        user.addLinkedAccount(guild, userId)
        return this.updateUser(user)
    }

    suspend fun removeLinkedAccount(guild: Guild, user: GuildMember, userId: String): GuildMember {
        user.removeLinkedAccount(guild, userId)
        return this.updateUser(user)
    }

    suspend fun cleanseNotes(guild: Guild, user: GuildMember): GuildMember {
        user.cleanseNotes(guild)
        return this.updateUser(user)
    }

    suspend fun addInfraction(guild: Guild, user: GuildMember, infraction: Infraction): Infraction {
        user.addInfraction(infraction, guild)
        infraction.punishment = getPunishmentForPoints(guild, user)
        user.updatePointDecayDate(guild, infraction.punishment?.duration ?: 0)
        this.updateUser(user)
        return infraction
    }

    suspend fun addMessageDelete(guild: Guild, user: GuildMember, deleteReaction: Boolean): GuildMember {
        user.addMessageDeleted(guild, deleteReaction)
        return this.updateUser(user)
    }

    suspend fun cleanseInfractions(guild: Guild, user: GuildMember): GuildMember {
        user.cleanseInfractions(guild)
        return this.updateUser(user)
    }

    suspend fun removeInfraction(guild: Guild, user: GuildMember, infractionId: Int): GuildMember {
        user.deleteInfraction(guild, infractionId)
        return this.updateUser(user)
    }

    suspend fun incrementUserHistory(user: GuildMember, guild: Guild): GuildMember {
        user.incrementHistoryCount(guild.id.toString())
        return this.updateUser(user)
    }

    suspend fun resetUserRecord(guild: Guild, user: GuildMember): GuildMember {
        user.reset(guild)
        return this.updateUser(user)
    }

    suspend fun updatePointDecayState(guild: Guild, user: GuildMember, freeze: Boolean): GuildMember {
        user.updatePointDecayState(guild, freeze)
        return this.updateUser(user)
    }

    suspend fun enableThinIceMode(guild: Guild, user: GuildMember): GuildMember {
        user.enableThinIce(guild)
        return this.updateUser(user)
    }

    suspend fun addBanRecord(guild: Guild, user: GuildMember, ban: Ban): GuildMember {
        user.addBan(guild, ban)
        return this.updateUser(user)
    }

    suspend fun addUnbanRecord(guild: Guild, user: GuildMember, thinIce: Boolean): GuildMember {
        user.unban(guild, thinIce, configuration[guild.id]!!.infractionConfiguration.warnUpgradeThreshold)
        return this.updateUser(user)
    }

    private suspend fun updateUser(user: GuildMember): GuildMember {
        userCollection.updateOne(GuildMember::userId eq user.userId, user)
        return user
    }

    private fun getPunishmentForPoints(guild: Guild, guildMember: GuildMember): PunishmentLevel {
        val punishmentLevels = configuration[guild.id]?.punishments
        return punishmentLevels!!.filter {
            it.points <= guildMember.getGuildInfo(guild.id.toString()).points
        }.maxByOrNull { it.points }!!
    }
}