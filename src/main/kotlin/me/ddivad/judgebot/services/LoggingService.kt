package me.ddivad.judgebot.services

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.behavior.getChannelOf
import com.gitlab.kordlib.core.entity.Guild
import com.gitlab.kordlib.core.entity.Role
import com.gitlab.kordlib.core.entity.User
import com.gitlab.kordlib.core.entity.channel.TextChannel
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.dataclasses.Infraction
import me.jakejmattson.discordkt.api.annotations.Service

@Service
class LoggingService(private val configuration: Configuration) {

    suspend fun roleApplied(guild: Guild, user: User, role: Role) =
        log(guild, "**Info ::** Role ${role.mention} :: ${role.id.value} added to ${user.mention} :: ${user.tag}")

    suspend fun roleRemoved(guild: Guild, user: User, role: Role) =
            log(guild, "**Info ::** Role ${role.mention} :: ${role.id.value} removed from ${user.mention} :: ${user.tag}")

    suspend fun rejoinMute(guild: Guild, user: User, roleState: RoleState) =
            log(guild, "**Info ::** User ${user.mention} :: ${user.tag} joined the server with ${if(roleState == RoleState.Tracked) "an infraction" else "a manual"} mute remaining")

    suspend fun infractionApplied(guild: Guild, user: User, infraction: Infraction) {
        val moderator = guild.kord.getUser(Snowflake(infraction.moderator))
        log(guild, "**Info ::** User ${user.mention} :: ${user.tag} was infracted by **${moderator?.username} :: ${moderator?.tag}** \nReason: **${infraction.reason}**")
    }

    private suspend fun log(guild: Guild, message: String) = getLoggingChannel(guild)?.createMessage(message)

    private suspend fun getLoggingChannel(guild: Guild): TextChannel? {
        val channelId = configuration[guild.id.longValue]?.loggingConfiguration?.loggingChannel.takeIf { it!!.isNotEmpty() } ?: return null
        return guild.getChannelOf<TextChannel>(Snowflake(channelId))
    }
}