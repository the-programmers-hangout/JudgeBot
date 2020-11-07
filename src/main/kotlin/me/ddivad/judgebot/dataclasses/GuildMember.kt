package me.ddivad.judgebot.dataclasses

import com.gitlab.kordlib.core.entity.Guild
import org.joda.time.DateTime
import org.joda.time.Weeks

data class GuildMemberDetails(
        val guildId: String,
        val notes: MutableList<Note> = mutableListOf(),
        val infractions: MutableList<Infraction> = mutableListOf(),
        val leaveHistory: MutableList<GuildLeave> = mutableListOf(),
        var historyCount: Int = 0,
        var points: Int = 0,
        var pointDecayTimer: Long = DateTime().millis,
        var lastInfraction: Long = 0,
)

data class GuildLeave(
        val joinDate: Long?,
        var leaveDate: Long?
)

data class GuildMember(
        val userId: String,
        val guilds: MutableList<GuildMemberDetails> = mutableListOf()
) {
    fun addNote(note: String, moderator: String, guild: Guild) = with(this.getGuildInfo(guild.id.value)) {
        val nextId: Int = if (this.notes.isEmpty()) 1 else this.notes.maxByOrNull { it.id }!!.id + 1
        this.notes.add(Note(note, moderator, DateTime().millis, nextId))
    }

    fun deleteNote(noteId: Int, guild: Guild) = with(this.getGuildInfo(guild.id.value)) {
        this.notes.removeIf { it.id == noteId }
    }

    fun cleanseNotes(guild: Guild) = with(this.getGuildInfo(guild.id.value)) {
        this.notes.clear()
    }

    fun cleanseInfractions(guild: Guild) = with(this.getGuildInfo(guild.id.value)) {
        this.infractions.clear()
        this.points = 0
    }

    fun deleteInfraction(guild: Guild, infractionId: Int) = with(this.getGuildInfo(guild.id.value)) {
        this.infractions.find { it.id == infractionId }?.let {
            this.infractions.remove(it)
            this.points -= it.points
        }
    }

    fun addInfraction(infraction: Infraction, guild: Guild) = with(this.getGuildInfo(guild.id.value)) {
        val nextId: Int = if (this.infractions.isEmpty()) 1 else this.infractions.maxByOrNull { it.id!! }?.id!! + 1
        infraction.id = nextId
        this.infractions.add(infraction)
        this.points += infraction.points
        this.lastInfraction = DateTime().millis
    }

    fun incrementHistoryCount(guildId: String) {
        this.getGuildInfo(guildId).historyCount += 1
    }

    fun addGuildLeave( guild: Guild, leaveDate: Long) = with(this.getGuildInfo(guild.id.value)) {
        val joinRecord = this.leaveHistory.find { it.joinDate != null && it.leaveDate == null }?.let {
            it.leaveDate = leaveDate
        }
    }

    fun addGuildJoinDate(guild: Guild, joinDate: Long) = with(this.getGuildInfo(guild.id.value)){
        this.leaveHistory.add(GuildLeave(joinDate, null))
    }

    fun checkPointDecay(guild: Guild, configuration: GuildConfiguration) = with(this.getGuildInfo(guild.id.value)) {
        val weeksSincePointsDecayed = Weeks.weeksBetween(DateTime(this.pointDecayTimer), DateTime()).weeks
        if (weeksSincePointsDecayed > 0) {
            val pointsToRemove = configuration.infractionConfiguration.pointDecayPerWeek * weeksSincePointsDecayed
            this.points -= pointsToRemove
            if (this.points < 0) this.points = 0
            this.pointDecayTimer = DateTime().millis
        }
    }

    fun getPoints(guild: Guild) = with(this.getGuildInfo(guild.id.value)) {
        return@with this.points
    }

    fun ensureGuildDetailsPresent(guildId: String) {
        if (this.guilds.any { it.guildId == guildId }) return
        this.guilds.add(GuildMemberDetails(guildId))
    }

    fun getGuildInfo(guildId: String): GuildMemberDetails {
        return this.guilds.firstOrNull { it.guildId == guildId } ?: GuildMemberDetails(guildId)
    }
}