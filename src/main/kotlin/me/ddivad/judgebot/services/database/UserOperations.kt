package me.ddivad.judgebot.services.database

import com.gitlab.kordlib.core.entity.Guild
import com.gitlab.kordlib.core.entity.Member
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
            return if(userRecord != null) {
                userRecord.ensureGuildDetailsPresent(guild.id.value)
                userRecord.checkPointDecay(guild, configuration[guild.id.longValue]!!)
                userRecord
            } else {
                val guildMember = GuildMember(target.id.value)
                guildMember.guilds.add(GuildMemberDetails(guild.id.value))
                userCollection.insertOne(guildMember)
                guildMember
            }
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

    suspend fun addInfraction(guild: Guild, user: GuildMember, infraction: Infraction): GuildMember {
        user.addInfraction(infraction, guild)
        return this.updateUser(user)
    }

    suspend fun cleanseInfractions(guild: Guild, user: GuildMember): GuildMember {
        user.cleanseInfractions(guild)
        return this.updateUser(user)
    }

    suspend fun incrementUserHistory(user: GuildMember, guild: Guild): GuildMember {
        user.incrementHistoryCount(guild.id.value)
        return this.updateUser(user)
    }

    suspend fun insertGuildLeave(user: GuildMember, guild: Guild, joinDateTime: Long, leaveDateTime: Long): GuildMember {
        user.addGuildLeave(joinDateTime, leaveDateTime, guild)
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