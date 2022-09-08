package me.ddivad.judgebot.services.infractions

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.common.exception.RequestException
import dev.kord.core.behavior.edit
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Member
import dev.kord.core.entity.PermissionOverwrite
import dev.kord.core.entity.User
import dev.kord.core.supplier.EntitySupplyStrategy
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.toKotlinInstant
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.dataclasses.InfractionType
import me.ddivad.judgebot.dataclasses.Punishment
import me.ddivad.judgebot.embeds.createMuteEmbed
import me.ddivad.judgebot.embeds.createUnmuteEmbed
import me.ddivad.judgebot.services.DatabaseService
import me.ddivad.judgebot.services.LoggingService
import me.ddivad.judgebot.util.applyRoleWithTimer
import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.annotations.Service
import me.jakejmattson.discordkt.extensions.sendPrivateMessage
import me.jakejmattson.discordkt.extensions.toSnowflake
import mu.KotlinLogging
import java.time.Instant
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlin.time.toJavaDuration

typealias GuildID = String
typealias UserId = String

enum class MuteState {
    None,
    Tracked,
    Untracked,
    TimedOut
}

@Service
class MuteService(
    val configuration: Configuration,
    private val discord: Discord,
    private val databaseService: DatabaseService,
    private val loggingService: LoggingService
) {
    private val logger = KotlinLogging.logger { }
    private val muteTimerMap = hashMapOf<Pair<UserId, GuildID>, Job>()
    private suspend fun getMutedRole(guild: Guild) = guild.getRole(configuration[guild.id]?.mutedRole!!)
    private fun toKey(user: User, guild: Guild) = user.id.toString() to guild.id.toString()
    suspend fun initGuilds() {
        configuration.guildConfigurations.forEach { config ->
            runBlocking {
                try {
                    val guild = config.key.let { discord.kord.getGuild(it) } ?: return@runBlocking
                    initialiseMuteTimers(guild)
                    setupMutedRole(guild)
                } catch (ex: Exception) {
                    println(ex.message)
                }
            }
        }
    }

    suspend fun applyInfractionMute(member: Member, time: Long) {
        applyMute(member, time)
    }

    suspend fun applyMuteAndSendReason(member: Member, time: Long, reason: String) {
        val guild = member.guild.asGuild()
        val user = member.asUser()
        applyMute(member, time)
        try {
            member.sendPrivateMessage {
                createMuteEmbed(guild, member, reason, time)
            }
        } catch (ex: RequestException) {
            loggingService.dmDisabled(guild, user)
        }
    }

    private suspend fun applyMute(member: Member, time: Long) {
        val guild = member.guild.asGuild()
        val user = member.asUser()
        val clearTime = Instant.now().plus(time.toDuration(DurationUnit.MILLISECONDS).toJavaDuration()).toEpochMilli()
        val punishment = Punishment(user.id.toString(), InfractionType.Mute, clearTime)
        val muteRole = getMutedRole(guild)
        val timeoutDuration = Instant.ofEpochMilli(Instant.now().toEpochMilli() + time - 2000)
        val key = toKey(user, guild)
        if (key in muteTimerMap) {
            muteTimerMap[key]?.cancel()
            muteTimerMap.remove(key)
            databaseService.guilds.removePunishment(guild, member.asUser().id.toString(), InfractionType.Mute)
            loggingService.muteOverwritten(guild, member)
        }
        databaseService.guilds.addPunishment(guild.asGuild(), punishment)
        member.edit { communicationDisabledUntil = timeoutDuration.toKotlinInstant() }
        muteTimerMap[key] = applyRoleWithTimer(member, muteRole, time) {
            removeMute(guild, user)
        }.also {
            loggingService.roleApplied(guild, member.asUser(), muteRole)
        }
    }

    suspend fun gag(guild: Guild, target: Member, moderator: User) {
        val muteDuration = configuration[guild.id]?.infractionConfiguration?.gagDuration ?: return
        loggingService.gagApplied(guild, target, moderator)
        this.applyMuteAndSendReason(target, muteDuration, "You've been muted temporarily by staff.")
    }

    fun removeMute(guild: Guild, user: User) {
        runBlocking {
            val muteRole = getMutedRole(guild)
            val key = toKey(user, guild)
            guild.getMemberOrNull(user.id)?.let {
                it.removeRole(muteRole.id)
                it.edit { communicationDisabledUntil = Instant.now().toKotlinInstant() }
                try {
                    it.sendPrivateMessage {
                        createUnmuteEmbed(guild, user)
                    }
                } catch (ex: RequestException) {
                    loggingService.dmDisabled(guild, user)
                }
                loggingService.roleRemoved(guild, user, muteRole)
                if (checkMuteState(guild, it) == MuteState.Untracked) return@runBlocking
            }
            databaseService.guilds.removePunishment(guild, user.id.toString(), InfractionType.Mute)
            muteTimerMap[key]?.cancel()
            muteTimerMap.remove(key)
        }
    }

    private suspend fun initialiseMuteTimers(guild: Guild) {
        runBlocking {
            val punishments = databaseService.guilds.getPunishmentsForGuild(guild, InfractionType.Mute)
            logger.info { "${guild.name} (${guild.id}): Existing Punishments :: ${punishments.size} existing punishments found for ${guild.name}" }
            punishments.forEach {
                if (it.clearTime != null) {
                    logger.info { "${guild.name} (${guild.id}): Adding Existing Timer :: UserId: ${it.userId}, GuildId: ${guild.id.value}, PunishmentId: ${it.id}" }
                    val difference = it.clearTime - Instant.now().toEpochMilli()
                    val member = guild.getMemberOrNull(it.userId.toSnowflake()) ?: return@forEach
                    val user = member.asUser()
                    val key = toKey(user, guild)
                    muteTimerMap[key] = applyRoleWithTimer(member, getMutedRole(guild), difference) {
                        removeMute(guild, user)
                    }
                }
            }
        }
        loggingService.initialiseMutes(guild, getMutedRole(guild))
    }

    suspend fun handleRejoinMute(guild: Guild, member: Member) {
        val mute = databaseService.guilds.checkPunishmentExists(guild, member, InfractionType.Mute).first()
        if (mute.clearTime != null) {
            val difference = mute.clearTime - Instant.now().toEpochMilli()
            val user = member.asUser()
            val key = toKey(user, guild)
            muteTimerMap[key] = applyRoleWithTimer(member, getMutedRole(guild), difference) {
                removeMute(guild, user)
            }
        }
    }

    suspend fun checkMuteState(guild: Guild, member: Member): MuteState {
        return if (databaseService.guilds.checkPunishmentExists(guild, member, InfractionType.Mute)
                .isNotEmpty()
        ) MuteState.Tracked
        else if (member.roles.toList().contains(getMutedRole(member.getGuild()))) MuteState.Untracked
        else if (member.communicationDisabledUntil != null && member.communicationDisabledUntil!! > Instant.now()
                .toKotlinInstant()
        ) {
            MuteState.TimedOut
        } else MuteState.None
    }

    suspend fun setupMutedRole(guild: Guild) {
        val mutedRole = guild.getRole(configuration[guild.id]!!.mutedRole)
        guild.withStrategy(EntitySupplyStrategy.cachingRest).channels.toList().forEach {
            val deniedPermissions = it.getPermissionOverwritesForRole(mutedRole.id)?.denied ?: Permissions()
            if (deniedPermissions.values.any { permission ->
                    permission in setOf(
                        Permission.SendMessages,
                        Permission.AddReactions,
                        Permission.CreatePublicThreads,
                        Permission.CreatePrivateThreads,
                        Permission.SendMessagesInThreads
                    )
                }) {
                try {
                    it.addOverwrite(
                        PermissionOverwrite.forRole(
                            mutedRole.id,
                            denied = deniedPermissions.plus(Permission.SendMessages).plus(Permission.AddReactions)
                                .plus(Permission.CreatePrivateThreads).plus(Permission.CreatePrivateThreads)
                                .plus(Permission.SendMessagesInThreads)
                        ),
                        "Judgebot Overwrite"
                    )
                } catch (ex: RequestException) {
                    logger.warn { "${guild.name} (${guild.id}): No permssions to add overwrite to ${it.id.value} - ${it.name}" }
                }
            }
        }
    }
}
