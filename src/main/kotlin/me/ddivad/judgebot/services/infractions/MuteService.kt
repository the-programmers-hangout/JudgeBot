package me.ddivad.judgebot.services.infractions

import com.gitlab.kordlib.common.exception.RequestException
import com.gitlab.kordlib.common.entity.Permission
import com.gitlab.kordlib.common.entity.Permissions
import com.gitlab.kordlib.core.entity.Guild
import com.gitlab.kordlib.core.entity.Member
import com.gitlab.kordlib.core.entity.PermissionOverwrite
import com.gitlab.kordlib.core.entity.User
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
    private val muteTimerMap = hashMapOf<Pair<UserId, GuildID>, Job>()
    private suspend fun getMutedRole(guild: Guild) = guild.getRole(configuration[guild.id.longValue]?.mutedRole?.toSnowflake()!!)
    private fun toKey(user: User, guild: Guild) = user.id.value to guild.id.value
    suspend fun initGuilds() {
        configuration.guildConfigurations.forEach { config ->
            val guild = config.value.id.toSnowflake().let { discord.api.getGuild(it) } ?: return@forEach
            initialiseMuteTimers(guild)
            setupMutedRole(guild)
        }
    }

    suspend fun applyMute(member: Member, time: Long, reason: String) {
        val guild = member.guild.asGuild()
        val user = member.asUser()
        val clearTime = DateTime.now().plus(time).millis
        val punishment = Punishment(user.id.value, InfractionType.Mute, reason, "", clearTime)
        val muteRole = getMutedRole(guild)
        val key = toKey(user, guild)
        if (key in muteTimerMap) {
            muteTimerMap[key]?.cancel()
            muteTimerMap.remove(key)
            databaseService.guilds.removePunishment(guild, member.asUser().id.value, InfractionType.Mute)
            loggingService.muteOverwritten(guild, member)
        }
        databaseService.guilds.addPunishment(guild.asGuild(), punishment)
        muteTimerMap[key] = applyRoleWithTimer(member, muteRole, time) {
            removeMute(guild, user)
        }.also {
            loggingService.roleApplied(guild, member.asUser(), muteRole)
            member.sendPrivateMessage {
                createMuteEmbed(guild, member, reason, time)
            }
        }
    }

    suspend fun gag(target: Member) {
        this.applyMute(target, 1000L * 60 * 5, "You've been muted temporarily so that a mod can handle something.")
    }

    fun removeMute(guild: Guild, user: User) {
        runBlocking {
            val muteRole = getMutedRole(guild)
            val key = toKey(user, guild)
            guild.getMemberOrNull(user.id)?.let {
                it.removeRole(muteRole.id)
                it.sendPrivateMessage {
                    createUnmuteEmbed(guild, user)
                }
                loggingService.roleRemoved(guild, user, muteRole)
                if (checkRoleState(guild, it) == RoleState.Untracked) return@runBlocking
            }
            databaseService.guilds.removePunishment(guild, user.id.value, InfractionType.Mute)
            muteTimerMap[key]?.cancel()
            muteTimerMap.remove(key)
        }
    }

    private suspend fun initialiseMuteTimers(guild: Guild) {
        databaseService.guilds.getPunishmentsForGuild(guild, InfractionType.Mute).forEach {
            if (it.clearTime != null) {
                println("Adding Existing Timer :: UserId: ${it.userId}, GuildId: ${guild.id.value}, PunishmentId: ${it.id}")
                val difference = it.clearTime - DateTime.now().millis
                val member = guild.getMemberOrNull(it.userId.toSnowflake()) ?: return
                val user = member.asUser()
                val key = toKey(user, guild)
                muteTimerMap[key] = applyRoleWithTimer(member, getMutedRole(guild), difference) {
                    removeMute(guild, user)
                }
            }
            loggingService.initialiseMutes(guild, getMutedRole(guild))
        }
    }

    suspend fun handleRejoinMute(guild: Guild, member: Member) {
        val mute = databaseService.guilds.checkPunishmentExists(guild, member, InfractionType.Mute).first()
        if (mute.clearTime != null) {
            val difference = mute.clearTime - DateTime.now().millis
            val user = member.asUser()
            val key = toKey(user, guild)
            muteTimerMap[key] = applyRoleWithTimer(member, getMutedRole(guild), difference) {
                removeMute(guild, user)
            }
        }
    }

    suspend fun checkRoleState(guild: Guild, member: Member) = when {
        databaseService.guilds.checkPunishmentExists(guild, member, InfractionType.Mute).isNotEmpty() -> RoleState.Tracked
        member.roles.toList().contains(getMutedRole(member.getGuild())) -> RoleState.Untracked
        else -> RoleState.None
    }

    private suspend fun setupMutedRole(guild: Guild) {
        val mutedRole = guild.getRole(configuration[guild.id.longValue]!!.mutedRole.toSnowflake())
        guild.channels.toList().forEach {
            val deniedPermissions = it.getPermissionOverwritesForRole(mutedRole.id)?.denied ?: Permissions()
            if (!deniedPermissions.contains(Permission.SendMessages) || !deniedPermissions.contains(Permission.AddReactions)) {
                try {
                    it.addOverwrite(
                            PermissionOverwrite.forRole(
                                    mutedRole.id,
                                    denied = deniedPermissions.plus(Permission.SendMessages).plus(Permission.AddReactions))
                    )
                } catch (ex: RequestException) {
                    println("No permssions to add overwrite to ${it.id.value} - ${it.name}")
                }

            }
        }
    }
}
