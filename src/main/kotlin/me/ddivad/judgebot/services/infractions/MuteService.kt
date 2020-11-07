package me.ddivad.judgebot.services.infractions

import com.gitlab.kordlib.common.entity.Permission
import com.gitlab.kordlib.common.entity.Permissions
import com.gitlab.kordlib.core.entity.Guild
import com.gitlab.kordlib.core.entity.Member
import com.gitlab.kordlib.core.entity.PermissionOverwrite
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.dataclasses.InfractionType
import me.ddivad.judgebot.dataclasses.Punishment
import me.ddivad.judgebot.embeds.createMuteEmbed
import me.ddivad.judgebot.embeds.createUnmuteEmbed
import me.ddivad.judgebot.services.DatabaseService
import me.ddivad.judgebot.services.LoggingService
import me.ddivad.judgebot.util.applyRoleWithTimer
import me.jakejmattson.discordkt.api.Discord
import me.jakejmattson.discordkt.api.annotations.Service
import me.jakejmattson.discordkt.api.extensions.sendPrivateMessage
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

    suspend fun initGuilds() {
        configuration.guildConfigurations.forEach { config ->
            val guild = config.value.id.toSnowflake().let { discord.api.getGuild(it) } ?: return@forEach
            handleExistingMutes(guild)
            setupMutedRole(guild)
        }
    }

    suspend fun applyMute(member: Member, time: Long, reason: String) {
        val guild = member.guild.asGuild()
        val userId = member.asUser().id.value
        val key = toKey(member)
        val clearTime = DateTime.now().plus(time).millis
        val muteRole = getMutedRole(guild)

        if (key in punishmentTimerMap) {
            punishmentTimerMap[key]?.cancel()
            punishmentTimerMap.remove(key)
            databaseService.guilds.removePunishment(guild, member.asUser().id.value, InfractionType.Mute)
            loggingService.muteOverwritten(guild, member)
        }
        val punishment = Punishment(userId, InfractionType.Mute, reason, "", clearTime)
        databaseService.guilds.addPunishment(guild.asGuild(), punishment)
        punishmentTimerMap[key] = applyRoleWithTimer(member, muteRole, time) {
            removeMute(member)
        }.also {
            loggingService.roleApplied(guild, member.asUser(), muteRole)
            member.sendPrivateMessage {
                createMuteEmbed(guild, member, reason, time)
            }
        }
    }

    fun removeMute(member: Member) {
        runBlocking {
            val guild = member.guild.asGuild()
            val key = toKey(member)
            val muteRole = getMutedRole(guild)
            member.removeRole(muteRole.id)
            databaseService.guilds.removePunishment(guild, member.asUser().id.value, InfractionType.Mute)
            punishmentTimerMap[key]?.cancel()
            punishmentTimerMap.remove(toKey(member))
            member.sendPrivateMessage {
                createUnmuteEmbed(guild, member)
            }
            loggingService.roleRemoved(guild, member.asUser(), muteRole)
        }
    }

    private suspend fun handleExistingMutes(guild: Guild) {
        databaseService.guilds.getPunishmentsForGuild(guild, InfractionType.Mute).forEach {
            if (it.clearTime != null) {
                val difference = it.clearTime - DateTime.now().millis
                val member = guild.getMemberOrNull(it.userId.toSnowflake()) ?: return
                val key = toKey(member)
                punishmentTimerMap[key] = applyRoleWithTimer(member, getMutedRole(guild), difference) {
                    removeMute(member)
                }
            }
        }
    }

    suspend fun handleRejoinMute(guild: Guild, member: Member) {
        val mute = databaseService.guilds.checkPunishmentExists(guild, member, InfractionType.Mute).first()
        if (mute.clearTime != null) {
            val difference = mute.clearTime - DateTime.now().millis
            val key = toKey(member)
            punishmentTimerMap[key] = applyRoleWithTimer(member, getMutedRole(guild), difference) {
                removeMute(member)
            }
        }
    }

    suspend fun checkRoleState(guild: Guild, member: Member, type: InfractionType) = when {
        databaseService.guilds.checkPunishmentExists(guild, member, type).isNotEmpty() -> RoleState.Tracked
        member.roles.toList().contains(getMutedRole(member.getGuild())) -> RoleState.Untracked
        else -> RoleState.None
    }

    private suspend fun setupMutedRole(guild: Guild) {
        val mutedRole = guild.getRole(configuration[guild.id.longValue]!!.mutedRole.toSnowflake())
        loggingService.muteSetup(guild, mutedRole)
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