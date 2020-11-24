package me.ddivad.judgebot.services

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.behavior.getChannelOf
import com.gitlab.kordlib.core.entity.Guild
import com.gitlab.kordlib.core.entity.Member
import com.gitlab.kordlib.core.entity.Role
import com.gitlab.kordlib.core.entity.User
import com.gitlab.kordlib.core.entity.channel.TextChannel
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.dataclasses.Infraction
import me.ddivad.judgebot.dataclasses.Punishment
import me.ddivad.judgebot.services.infractions.RoleState
import me.ddivad.judgebot.util.timeBetween
import me.jakejmattson.discordkt.api.annotations.Service
import org.joda.time.DateTime

@Service
class LoggingService(private val configuration: Configuration) {

    suspend fun roleApplied(guild: Guild, user: User, role: Role) =
            log(guild, "**Info ::** Role ${role.mention} :: ${role.id.value} added to ${user.mention} :: ${user.tag}")

    suspend fun muteOverwritten(guild: Guild, user: User) =
            log(guild, "**Info ::** User ${user.mention} :: ${user.tag} had an active mute, but has received another mute. Active mute will be replaced.")

    suspend fun roleRemoved(guild: Guild, user: User, role: Role) =
            log(guild, "**Info ::** Role ${role.mention} :: ${role.id.value} removed from ${user.mention} :: ${user.tag}")

    suspend fun rejoinMute(guild: Guild, user: User, roleState: RoleState) =
            log(guild, "**Info ::** User ${user.mention} :: ${user.tag} joined the server with ${if (roleState == RoleState.Tracked) "an infraction" else "a manual"} mute remaining")

    suspend fun channelOverrideAdded(guild: Guild, channel: TextChannel) =
            log(guild, "**Info ::** Channel overrides for muted role added to ${channel.name}")

    suspend fun userBanned(guild: Guild, user: User, ban: Punishment) {
        val moderator = guild.kord.getUser(Snowflake(ban.moderator))
        log(guild, "**Info ::** User ${user.mention} :: ${user.tag} banned by ${moderator?.username} for reason: ${ban.reason}")
    }

    suspend fun userUnbanned(guild: Guild, user: User) =
            log(guild, "**Info ::** User ${user.mention} :: ${user.tag} unbanned")

    suspend fun userBannedWithTimer(guild: Guild, user: User, punishment: Punishment) {
        val moderator = guild.kord.getUser(Snowflake(punishment.moderator))

        log(guild, "**Info ::** User ${user.mention} :: ${user.tag} **temporarily** banned by ${moderator?.username} for reason: ${punishment.reason}. Unban scheduled in ${timeBetween(DateTime(punishment.clearTime))}")
    }


    suspend fun infractionApplied(guild: Guild, user: User, infraction: Infraction) {
        val moderator = guild.kord.getUser(Snowflake(infraction.moderator))
        log(guild, "**Info ::** User ${user.mention} :: ${user.tag} was infracted (**${infraction.type}**) by **${moderator?.username} :: ${moderator?.tag}** \n**Reason**: ${infraction.reason}")
    }

    suspend fun badBfpApplied(guild: Guild, user: Member) =
            log(guild, "**Info ::** User ${user.mention} badPfp triggered for avatar <${user.avatar.url}>")

    suspend fun badPfpCancelled(guild: Guild, user: Member) =
            log(guild, "**Info ::** BadPfp cancelled for user ${user.mention}")

    suspend fun badPfpBan(guild: Guild, user: Member) =
            log(guild, "**Info ::** User ${user.mention} banned for not changing their avatar")

    suspend fun initialiseMutes(guild: Guild, role: Role) =
            log(guild, "**Info ::** Existing mute timers initialized using ${role.mention} :: ${role.id.value}")

    suspend fun initialiseBans(guild: Guild) =
            log(guild, "**Info ::** Existing ban timers initialized.")

    suspend fun dmDisabled(guild: Guild, target: User) =
        log(guild, "**Error ::** Attempted to send direct message to ${target.mention} :: ${target.id} but they have DMs disabled")

    private suspend fun log(guild: Guild, message: String) = getLoggingChannel(guild)?.createMessage(message)

    private suspend fun getLoggingChannel(guild: Guild): TextChannel? {
        val channelId = configuration[guild.id.longValue]?.loggingConfiguration?.loggingChannel.takeIf { it!!.isNotEmpty() }
                ?: return null
        return guild.getChannelOf(Snowflake(channelId))
    }
}