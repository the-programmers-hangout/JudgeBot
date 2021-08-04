package me.ddivad.judgebot.dataclasses

import dev.kord.core.entity.Guild
import org.joda.time.DateTime
import org.joda.time.Weeks

data class GuildLeave(
    val joinDate: Long,
    var leaveDate: Long?
)

data class GuildMemberDetails(
    val guildId: String,
    val notes: MutableList<Note> = mutableListOf(),
    val infractions: MutableList<Infraction> = mutableListOf(),
    val info: MutableList<Info> = mutableListOf(),
    val linkedAccounts: MutableList<String> = mutableListOf(),
    var historyCount: Int = 0,
    var points: Int = 0,
    var pointDecayTimer: Long = DateTime().millis,
    var lastInfraction: Long = 0,
    val deletedMessageCount: DeletedMessages = DeletedMessages()
)

data class DeletedMessages(
    var deleteReaction: Int = 0,
    var total: Int = 0
)

data class GuildMember(
        val userId: String,
        val guilds: MutableList<GuildMemberDetails> = mutableListOf()
) {
    fun addNote(note: String, moderator: String, guild: Guild) = with(this.getGuildInfo(guild.id.asString)) {
        val nextId: Int = if (this.notes.isEmpty()) 1 else this.notes.maxByOrNull { it.id }!!.id + 1
        this.notes.add(Note(note, moderator, DateTime().millis, nextId))
    }

    fun editNote(guild: Guild, noteId: Int, newNote: String, moderator: String) = with(this.getGuildInfo(guild.id.asString)) {
        this.notes.find { it.id == noteId }?.let{
            it.note = newNote
            it.moderator = moderator
        }
    }

    fun deleteNote(noteId: Int, guild: Guild) = with(this.getGuildInfo(guild.id.asString)) {
        this.notes.removeIf { it.id == noteId }
    }

    fun addInfo(information: Info, guild: Guild) = with(this.getGuildInfo(guild.id.asString)) {
        val nextId: Int = if (this.info.isEmpty()) 1 else this.info.maxByOrNull { it.id!! }!!.id!! + 1
        information.id = nextId
        this.info.add(information)
    }

    fun removeInfo(id: Int, guild: Guild) = with(this.getGuildInfo(guild.id.asString)) {
        this.info.removeIf { it.id == id }
    }

    fun addLinkedAccount(guild: Guild, userId: String) = with(this.getGuildInfo(guild.id.asString)) {
        this.linkedAccounts.find { it == userId }.let {
            if (it == null) {
                this.linkedAccounts.add(userId)
            }
            return@let
        }
    }

    fun getLinkedAccounts(guild: Guild) = with(this.getGuildInfo(guild.id.asString)) {
        this.linkedAccounts
    }

    fun removeLinkedAccount(guild: Guild, userId: String) = with(this.getGuildInfo(guild.id.asString)) {
        this.linkedAccounts.removeIf { it == userId }
    }

    fun cleanseNotes(guild: Guild) = with(this.getGuildInfo(guild.id.asString)) {
        this.notes.clear()
    }

    private fun cleanseInfo(guild: Guild) = with(this.getGuildInfo(guild.id.asString)) {
        this.info.clear()
    }

    fun cleanseInfractions(guild: Guild) = with(this.getGuildInfo(guild.id.asString)) {
        this.infractions.clear()
        this.points = 0
    }

    fun deleteInfraction(guild: Guild, infractionId: Int) = with(this.getGuildInfo(guild.id.asString)) {
        this.infractions.find { it.id == infractionId }?.let {
            this.infractions.remove(it)
            this.points -= it.points
        }
    }

    fun addInfraction(infraction: Infraction, guild: Guild) = with(this.getGuildInfo(guild.id.asString)) {
        val nextId: Int = if (this.infractions.isEmpty()) 1 else this.infractions.maxByOrNull { it.id!! }?.id!! + 1
        infraction.id = nextId
        this.infractions.add(infraction)
        this.points += infraction.points
        this.pointDecayTimer = DateTime().millis.plus(infraction.punishment?.duration ?: 0)
        this.lastInfraction = DateTime().millis
    }

    fun incrementHistoryCount(guildId: String) {
        this.getGuildInfo(guildId).historyCount += 1
    }

    fun addMessageDeleted(guild: Guild, deleteReaction: Boolean) = with(this.getGuildInfo(guild.id.asString)) {
        this.deletedMessageCount.total++
        if (deleteReaction) this.deletedMessageCount.deleteReaction++
    }

    fun checkPointDecay(guild: Guild, configuration: GuildConfiguration) = with(this.getGuildInfo(guild.id.asString)) {
        val weeksSincePointsDecayed = Weeks.weeksBetween(DateTime(this.pointDecayTimer), DateTime()).weeks
        if (weeksSincePointsDecayed > 0) {
            val pointsToRemove = configuration.infractionConfiguration.pointDecayPerWeek * weeksSincePointsDecayed
            this.points -= pointsToRemove
            if (this.points < 0) this.points = 0
            this.pointDecayTimer = DateTime().millis
        }
    }

    fun getPoints(guild: Guild) = with(this.getGuildInfo(guild.id.asString)) {
        return@with this.points
    }

    fun reset(guild: Guild) = with(this.getGuildInfo(guild.id.asString)) {
        this.points = 0
        this.historyCount = 0
        this.deletedMessageCount.deleteReaction = 0
        cleanseInfo(guild)
        cleanseInfractions(guild)
        cleanseNotes(guild)
    }

    fun ensureGuildDetailsPresent(guildId: String) {
        if (this.guilds.any { it.guildId == guildId }) return
        this.guilds.add(GuildMemberDetails(guildId))
    }

    fun getGuildInfo(guildId: String): GuildMemberDetails {
        return this.guilds.firstOrNull { it.guildId == guildId } ?: GuildMemberDetails(guildId)
    }
}