package me.ddivad.judgebot.dataclasses

import com.gitlab.kordlib.core.entity.Guild
import org.joda.time.DateTime

data class GuildDetails(
        val guildId: String,
        val notes: MutableList<Note> = mutableListOf<Note>(),
        val infractions: MutableList<Infraction> = mutableListOf<Infraction>(),
        var historyCount: Int = 0,
        var points: Int = 0,
        var lastInfraction: Long = 0
)

data class GuildMember(
        val userId: String,
        val guilds: MutableList<GuildDetails> = mutableListOf<GuildDetails>()
) {
    fun addNote(note: String, moderator: String, guild: Guild) = with(this.getGuildInfo(guild.id.value)) {
        val nextId: Int = if (this!!.notes.isEmpty()) 1 else this.notes.maxBy { it.id }!!.id + 1
        this.notes.add(Note(note, moderator, DateTime.now().millis, nextId))
    }

    fun deleteNote(noteId: Int, guild: Guild) = with(this.getGuildInfo(guild.id.value)) {
        this?.notes?.removeIf { it.id == noteId }
    }

    fun addInfraction(infraction: Infraction, guild:Guild) = with(this.getGuildInfo(guild.id.value)) {
        this?.infractions?.add(infraction)
    }

    fun incrementHistoryCount(guildId: String) {
        this.getGuildInfo(guildId)!!.historyCount += 1
    }

    fun ensureGuildDetailsPresent(guildId: String) {
        if (this.guilds.any { it.guildId == guildId }) return
        this.guilds.add(GuildDetails(guildId))
    }

    fun getGuildInfo(guildId: String): GuildDetails? {
        return this.guilds.firstOrNull { it.guildId == guildId }
    }
}