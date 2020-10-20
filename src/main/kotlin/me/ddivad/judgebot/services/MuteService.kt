package me.ddivad.judgebot.services

import com.gitlab.kordlib.common.entity.Permission
import com.gitlab.kordlib.common.entity.Permissions
import com.gitlab.kordlib.core.entity.Guild
import com.gitlab.kordlib.core.entity.Member
import com.gitlab.kordlib.core.entity.PermissionOverwrite
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.dataclasses.InfractionType
import me.ddivad.judgebot.dataclasses.Punishment
import me.ddivad.judgebot.util.applyRoleWithTimer
import me.jakejmattson.discordkt.api.Discord
import me.jakejmattson.discordkt.api.annotations.Service
import me.jakejmattson.discordkt.api.extensions.toSnowflake
import org.joda.time.DateTime

typealias GuildID = String
typealias UserId = String

enum class RoleState {
    None,
    Tracked,
    Untracked,
}

@Service
class MuteService(val configuration: Configuration,
                  private val discord: Discord,
                  private val databaseService: DatabaseService,
                  private val loggingService: LoggingService) {
    private val punishmentTimerMap = hashMapOf<Pair<GuildID, UserId>, Job>()
    private suspend fun toKey(member: Member) = member.guild.id.value to member.asUser().id.value
    private suspend fun getMutedRole(guild: Guild) = guild.getRole(configuration[guild.id.longValue]?.mutedRole?.toSnowflake()!!)

    init {
        runBlocking {
            configuration.guildConfigurations.forEach { config ->
                val guild = config.value.id.toSnowflake()?.let { discord.api.getGuild(it) } ?: return@forEach
                handleExistingMutes(guild)
                setupMutedRole(guild)
            }
        }
    }

    suspend fun applyMute(member: Member, time: Long, reason: String, type: InfractionType) {
        val guild = member.guild.asGuild()
        val userId = member.asUser().id.value
        val key = toKey(member)
        val clearTime = DateTime.now().plus(time).millis
        val muteRole = getMutedRole(guild)

        if (key in punishmentTimerMap) {
            punishmentTimerMap[key]?.cancel()
            punishmentTimerMap.remove(key)
        }
        val punishment = Punishment(userId, type, reason, clearTime)
        databaseService.guilds.addPunishment(guild.asGuild(), punishment)
        punishmentTimerMap[toKey(member)] = applyRoleWithTimer(member, muteRole, time) {
            removeMute(member, type)
        }.also { loggingService.roleApplied(guild, member.asUser(), muteRole) }
    }

    fun removeMute(member: Member, type: InfractionType) {
        runBlocking {
            val guild = member.guild.asGuild()
            val key = toKey(member)
            val muteRole = getMutedRole(guild)
            member.removeRole(muteRole.id)
            databaseService.guilds.removePunishment(guild, member.asUser().id.value, type)
            punishmentTimerMap[key]?.cancel()
            punishmentTimerMap.remove(toKey(member))
            loggingService.roleRemoved(guild, member.asUser(), muteRole)
        }
    }

    private suspend fun handleExistingMutes(guild: Guild) {
        databaseService.guilds.getPunishmentsForGuild(guild).forEach {
            val difference = it.clearTime - DateTime.now().millis
            val member = guild.getMemberOrNull(it.userId.toSnowflake()!!) ?: return
            applyRoleWithTimer(member, getMutedRole(guild), difference) { _ ->
                removeMute(member, it.type)
            }
        }
    }

    suspend fun handleRejoinMute(guild: Guild, member: Member) {
        val mute = databaseService.guilds.checkPunishmentExists(guild, member, InfractionType.Mute).first()
        val difference = mute.clearTime - DateTime.now().millis
        applyRoleWithTimer(member, getMutedRole(guild), difference) { _ ->
            removeMute(member, mute.type)
        }
    }

    suspend fun checkRoleState(guild: Guild, member: Member, type: InfractionType) = when {
        databaseService.guilds.checkPunishmentExists(guild, member, type).isNotEmpty() -> RoleState.Tracked
        member.roles.toList().contains(getMutedRole(member.getGuild())) -> RoleState.Untracked
        else -> RoleState.None
    }

    private suspend fun setupMutedRole(guild: Guild) {
        val mutedRole = guild.getRole(configuration[guild.id.longValue]!!.mutedRole.toSnowflake()!!)
        guild.channels.toList().forEach {
            val deniedPermissions = it.getPermissionOverwritesForRole(mutedRole.id)?.denied ?: Permissions()
            if (!deniedPermissions.contains(Permission.SendMessages) || !deniedPermissions.contains(Permission.AddReactions)) {
                it.addOverwrite(
                        PermissionOverwrite.forRole(
                                mutedRole.id,
                                denied = deniedPermissions.plus(Permission.SendMessages).plus(Permission.AddReactions))
                )
            }
        }
    }
}