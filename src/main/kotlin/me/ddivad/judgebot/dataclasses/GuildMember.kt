package me.ddivad.judgebot.dataclasses

import dev.kord.core.entity.Guild
import me.ddivad.judgebot.services.LoggingService
import me.jakejmattson.discordkt.extensions.TimeStamp
import mu.KotlinLogging
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.time.DurationUnit
import kotlin.time.toDuration
val logger = KotlinLogging.logger {  }

data class GuildMemberDetails(
    val guildId: String,
    val notes: MutableList<Note> = mutableListOf(),
    val infractions: MutableList<Infraction> = mutableListOf(),
    val info: MutableList<Info> = mutableListOf(),
    val linkedAccounts: MutableList<String> = mutableListOf(),
    val bans: MutableList<Ban> = mutableListOf(),
    var historyCount: Int = 0,
    var points: Int = 0,
    var pointDecayTimer: Long = Instant.now().toEpochMilli(),
    var lastInfraction: Long = 0,
    val deletedMessageCount: DeletedMessages = DeletedMessages(),
    var pointDecayFrozen: Boolean = false
)

data class DeletedMessages(
    var deleteReaction: Int = 0,
    var total: Int = 0
)

data class GuildMember(
    val userId: String,
    val guilds: MutableList<GuildMemberDetails> = mutableListOf()
) {

    fun addNote(note: String, moderator: String, guild: Guild) = with(this.getGuildInfo(guild.id.toString())) {
        val nextId: Int = if (this.notes.isEmpty()) 1 else this.notes.maxByOrNull { it.id }!!.id + 1
        this.notes.add(Note(note, moderator,Instant.now().toEpochMilli(), nextId))
    }

    fun editNote(guild: Guild, noteId: Int, newNote: String, moderator: String) =
        with(this.getGuildInfo(guild.id.toString())) {
            this.notes.find { it.id == noteId }?.let {
                it.note = newNote
                it.moderator = moderator
            }
        }

    fun deleteNote(noteId: Int, guild: Guild) = with(this.getGuildInfo(guild.id.toString())) {
        this.notes.removeIf { it.id == noteId }
    }

    fun addInfo(information: Info, guild: Guild) = with(this.getGuildInfo(guild.id.toString())) {
        val nextId: Int = if (this.info.isEmpty()) 1 else this.info.maxByOrNull { it.id!! }!!.id!! + 1
        information.id = nextId
        this.info.add(information)
    }

    fun removeInfo(id: Int, guild: Guild) = with(this.getGuildInfo(guild.id.toString())) {
        this.info.removeIf { it.id == id }
    }

    fun addLinkedAccount(guild: Guild, userId: String) = with(this.getGuildInfo(guild.id.toString())) {
        this.linkedAccounts.find { it == userId }.let {
            if (it == null) {
                this.linkedAccounts.add(userId)
            }
            return@let
        }
    }

    fun getLinkedAccounts(guild: Guild) = with(this.getGuildInfo(guild.id.toString())) {
        this.linkedAccounts
    }

    fun removeLinkedAccount(guild: Guild, userId: String) = with(this.getGuildInfo(guild.id.toString())) {
        this.linkedAccounts.removeIf { it == userId }
    }

    fun cleanseNotes(guild: Guild) = with(this.getGuildInfo(guild.id.toString())) {
        this.notes.clear()
    }

    private fun cleanseInfo(guild: Guild) = with(this.getGuildInfo(guild.id.toString())) {
        this.info.clear()
    }

    fun cleanseInfractions(guild: Guild) = with(this.getGuildInfo(guild.id.toString())) {
        this.infractions.clear()
        this.points = 0
    }

    fun deleteInfraction(guild: Guild, infractionId: Int) = with(this.getGuildInfo(guild.id.toString())) {
        this.infractions.find { it.id == infractionId }?.let {
            this.infractions.remove(it)
            this.points -= it.points
        }
    }

    fun addInfraction(infraction: Infraction, guild: Guild) = with(this.getGuildInfo(guild.id.toString())) {
        val nextId: Int = if (this.infractions.isEmpty()) 1 else this.infractions.maxByOrNull { it.id!! }?.id!! + 1
        infraction.id = nextId
        this.infractions.add(infraction)
        this.points += infraction.points
        this.lastInfraction = Instant.now().toEpochMilli()
    }

    fun incrementHistoryCount(guildId: String) {
        this.getGuildInfo(guildId).historyCount += 1
    }

    fun updatePointDecayDate(guild: Guild, punishmentDuration: Long) = with(this.getGuildInfo(guild.id.toString())) {
        this.pointDecayTimer = Instant.now().toEpochMilli().plus(punishmentDuration)
    }

    fun addMessageDeleted(guild: Guild, deleteReaction: Boolean) = with(this.getGuildInfo(guild.id.toString())) {
        this.deletedMessageCount.total++
        if (deleteReaction) this.deletedMessageCount.deleteReaction++
    }

    suspend fun checkPointDecay(guild: Guild, configuration: GuildConfiguration, loggingService: LoggingService) =
        with(this.getGuildInfo(guild.id.toString())) {
        if (this.pointDecayFrozen) {
            return@with
        }
        when {
            bans.lastOrNull()?.thinIce == true && Instant.now().toEpochMilli() >= this.pointDecayTimer -> {
                this.pointDecayTimer = Instant.now().toEpochMilli()
                this.pointDecayFrozen = false
            }
            else -> {
                val weeksSincePointsDecayed = (ChronoUnit.DAYS.between(Instant.ofEpochMilli(this.pointDecayTimer), Instant.now()) / 7).toInt()
                logger.debug { "Point decay: $weeksSincePointsDecayed - $points - $pointDecayTimer" }
                if (weeksSincePointsDecayed > 0 && this.points > 0) {
                    val pointsToRemove = configuration.infractionConfiguration.pointDecayPerWeek * weeksSincePointsDecayed
                    if (pointsToRemove > this.points) {
                        this.points = 0
                    } else {
                        this.points -= pointsToRemove
                        loggingService.pointDecayApplied(guild, this@GuildMember, this.points, pointsToRemove, weeksSincePointsDecayed)
                    }
                    this.pointDecayTimer = Instant.now().toEpochMilli()
                    logger.debug { "Point decay timer set to $pointDecayTimer" }
                }
            }
        }
    }

    fun updatePointDecayState(guild: Guild, frozen: Boolean) = with(this.getGuildInfo(guild.id.toString())) {
        this.pointDecayFrozen = frozen
        if (frozen) {
            addNote("Point decay frozen on ${TimeStamp.now()}", "${guild.kord.selfId}", guild)
        } else {
            this.pointDecayTimer = Instant.now().toEpochMilli()
        }
    }

    fun getPoints(guild: Guild) = with(this.getGuildInfo(guild.id.toString())) {
        return@with this.points
    }

    fun getTotalHistoricalPoints(guild: Guild) = with(this.getGuildInfo(guild.id.toString())) {
        return@with infractions.sumOf { it.points }
    }

    fun enableThinIce(guild: Guild) = with(this.getGuildInfo(guild.id.toString())) {
        this.points = 40
        this.pointDecayTimer = Instant.now().toEpochMilli().plus(60.toDuration(DurationUnit.DAYS).inWholeMilliseconds)
        this.pointDecayFrozen = true
    }

    fun addBan(guild: Guild, ban: Ban) = with(this.getGuildInfo(guild.id.toString())) {
        this.bans.add(ban)
    }

    fun unban(guild: Guild, thinIce: Boolean, thinIcePoints: Int) = with(this.getGuildInfo(guild.id.toString())) {
        this.bans.lastOrNull().let {
            it?.unbanTime = Instant.now().toEpochMilli()
            it?.thinIce = thinIce
        }
        if (thinIce) {
            enableThinIce(guild)
        }
        addNote(
            "Unbanned on ${TimeStamp.now()} ${
                if (thinIce) "with Thin Ice enabled. Points were set to $thinIcePoints and frozen until ${
                    TimeStamp.at(
                        Instant.ofEpochMilli(this.pointDecayTimer)
                    )
                }" else ""
            }", "${guild.kord.selfId}", guild
        )
    }

    fun reset(guild: Guild) = with(this.getGuildInfo(guild.id.toString())) {
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