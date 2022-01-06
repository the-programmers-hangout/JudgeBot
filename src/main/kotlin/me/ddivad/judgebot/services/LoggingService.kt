package me.ddivad.judgebot.services

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.entity.*
import dev.kord.core.entity.channel.TextChannel
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.dataclasses.GuildMember
import me.ddivad.judgebot.dataclasses.Infraction
import me.ddivad.judgebot.dataclasses.Punishment
import me.ddivad.judgebot.services.infractions.RoleState
import me.ddivad.judgebot.util.timeBetween
import me.jakejmattson.discordkt.annotations.Service
import me.jakejmattson.discordkt.extensions.descriptor
import me.jakejmattson.discordkt.extensions.pfpUrl
import org.joda.time.DateTime
import java.text.SimpleDateFormat
import java.util.*

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
            log(guild, "**Info ::** User ${user.mention} badPfp triggered for avatar <${user.pfpUrl}>")

    suspend fun badPfpCancelled(guild: Guild, user: Member) =
            log(guild, "**Info ::** BadPfp cancelled for user ${user.mention}")

    suspend fun badPfpBan(guild: Guild, user: Member) =
            log(guild, "**Info ::** User ${user.mention} banned for not changing their avatar")

    suspend fun initialiseMutes(guild: Guild, role: Role) =
            log(guild, "**Info ::** Existing mute timers initialized using ${role.mention} :: ${role.id.value}")

    suspend fun initialiseBans(guild: Guild) =
            log(guild, "**Info ::** Existing ban timers initialized.")

    suspend fun dmDisabled(guild: Guild, target: User) =
        log(guild, "**Info ::** Attempted to send direct message to ${target.mention} :: ${target.id.value} but they have DMs disabled")

    suspend fun gagApplied(guild: Guild, target: Member, moderator: User) =
        log(guild, "**Info ::** User ${target.mention} has been gagged by **${moderator.username} :: ${moderator.tag}**")

    suspend fun staffReactionUsed(guild: Guild, moderator: User, target: Member, reaction: ReactionEmoji) =
        log(guild, "**Info ::** ${reaction.name} used by ${moderator.username} on ${target.mention}")

    suspend fun pointDecayApplied(guild: Guild, target: GuildMember, newPoints: Int, pointsDeducted: Int, weeksSinceLastInfraction: Int) {
        val user = guild.kord.getUser(Snowflake(target.userId))

        log(
            guild,
            "**Info ::** Infraction Points for ${user?.descriptor()} " +
                    "reduced by **$pointsDeducted** to **$newPoints** " +
                    "for **$weeksSinceLastInfraction** infraction free weeks."
        )
    }

    private suspend fun log(guild: Guild, message: String) {
        getLoggingChannel(guild)?.createMessage(message)
        println("${SimpleDateFormat("dd/M/yyyy HH:mm:ss").format(Date())} > ${guild.name} > $message")
    }

    private suspend fun getLoggingChannel(guild: Guild): TextChannel? {
        val channelId = configuration[guild.id.value]?.loggingConfiguration?.loggingChannel.takeIf { it!!.isNotEmpty() }
                ?: return null
        return guild.getChannelOf(Snowflake(channelId))
    }
}