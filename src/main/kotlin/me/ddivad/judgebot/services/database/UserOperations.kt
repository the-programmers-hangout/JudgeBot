package me.ddivad.judgebot.services.database

import com.gitlab.kordlib.core.entity.Guild

import me.ddivad.judgebot.dataclasses.GuildMemberDetails
import me.ddivad.judgebot.dataclasses.GuildMember
import me.ddivad.judgebot.dataclasses.Infraction
import com.gitlab.kordlib.core.entity.User
import me.ddivad.judgebot.dataclasses.*
import me.jakejmattson.discordkt.api.annotations.Service
import org.litote.kmongo.eq

@Service
class UserOperations(private val connection: ConnectionService, private val configuration: Configuration) {
    private val userCollection = connection.db.getCollection<GuildMember>("Users")

    suspend fun getOrCreateUser(target: User, guild: Guild): GuildMember {
        val userRecord = userCollection.findOne(GuildMember::userId eq target.id.value)
        return if (userRecord != null) {
            userRecord.ensureGuildDetailsPresent(guild.id.value)
            userRecord.checkPointDecay(guild, configuration[guild.id.longValue]!!)
            userRecord
        } else {
            val guildMember = GuildMember(target.id.value)
            guildMember.guilds.add(GuildMemberDetails(guild.id.value))
            target.asMemberOrNull(guild.id)?.let { guildMember.addGuildJoinDate(guild, it.joinedAt.toEpochMilli()) }
            userCollection.insertOne(guildMember)
            guildMember
        }
    }

    suspend fun getUserOrNull(target: User, guild: Guild): GuildMember? {
        return userCollection.findOne(GuildMember::userId eq target.id.value)
    }

    suspend fun addNote(guild: Guild, user: GuildMember, note: String, moderator: String): GuildMember {
        user.addNote(note, moderator, guild)
        return this.updateUser(user)
    }

    suspend fun deleteNote(guild: Guild, user: GuildMember, noteId: Int): GuildMember {
        user.deleteNote(noteId, guild)
        return this.updateUser(user)
    }

    suspend fun cleanseNotes(guild: Guild, user: GuildMember): GuildMember {
        user.cleanseNotes(guild)
        return this.updateUser(user)
    }

    suspend fun addInfraction(guild: Guild, user: GuildMember, infraction: Infraction): Infraction {
        user.addInfraction(infraction, guild)
        infraction.punishment = getPunishmentForPoints(guild, user)
        this.updateUser(user)
        return infraction
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
        user.incrementHistoryCount(guild.id.value)
        return this.updateUser(user)
    }

    suspend fun addGuildLeave(user: GuildMember, guild: Guild, leaveDateTime: Long): GuildMember {
        user.addGuildLeave(guild, leaveDateTime)
        return this.updateUser(user)
    }

    suspend fun addGuildJoin(guild: Guild, user: GuildMember, joinDateTime: Long): GuildMember {
        user.addGuildJoinDate(guild, joinDateTime)
        return this.updateUser(user)
    }

    private suspend fun updateUser(user: GuildMember): GuildMember {
        userCollection.updateOne(GuildMember::userId eq user.userId, user)
        return user
    }

    private fun getPunishmentForPoints(guild: Guild, guildMember: GuildMember): PunishmentLevel {
        val punishmentLevels = configuration[guild.id.longValue]?.punishments
        return punishmentLevels!!.filter {
            it.points <= guildMember.getGuildInfo(guild.id.value).points
        }.maxByOrNull { it.points }!!
    }
}