package me.ddivad.judgebot.services

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.entity.*
import dev.kord.core.entity.channel.TextChannel
import me.ddivad.judgebot.dataclasses.Ban
import me.ddivad.judgebot.dataclasses.Configuration
import me.ddivad.judgebot.dataclasses.GuildMember
import me.ddivad.judgebot.dataclasses.Infraction
import me.ddivad.judgebot.services.infractions.MuteState
import me.jakejmattson.discordkt.annotations.Service
import me.jakejmattson.discordkt.extensions.descriptor
import me.jakejmattson.discordkt.extensions.idDescriptor
import me.jakejmattson.discordkt.extensions.pfpUrl
import mu.KotlinLogging

@Service
class LoggingService(private val configuration: Configuration) {
    val logger = KotlinLogging.logger {  }

    suspend fun roleApplied(guild: Guild, user: User, role: Role) {
        log(guild, "**Info ::** Role ${role.mention} :: ${role.id.value} added to ${user.mention} :: ${user.tag}")
        logger.info { buildGuildLogMessage(guild,"Role ${role.name} added to ${user.idDescriptor()}") }
    }

    suspend fun muteOverwritten(guild: Guild, user: User) =
        log(
            guild,
            "**Info ::** User ${user.mention} :: ${user.tag} had an active mute, but has received another mute. Active mute will be replaced."
        )

    suspend fun roleRemoved(guild: Guild, user: User, role: Role) {
        log(guild, "**Info ::** Role ${role.mention} :: ${role.id.value} removed from ${user.mention} :: ${user.tag}")
        logger.info { buildGuildLogMessage(guild,"Role ${role.name} removed from ${user.idDescriptor()}") }
    }

    suspend fun rejoinMute(guild: Guild, user: User, roleState: MuteState) =
        log(
            guild,
            "**Info ::** User ${user.mention} :: ${user.tag} joined the server with ${if (roleState == MuteState.Tracked) "an infraction" else "a manual"} mute remaining"
        )

    suspend fun channelOverrideAdded(guild: Guild, channel: TextChannel) {
        log(guild, "**Info ::** Channel overrides for muted role added to ${channel.name}")
        logger.info { buildGuildLogMessage(guild, "Channel override for muted role added to ${channel.name}") }
    }

    suspend fun userBanned(guild: Guild, user: User, ban: Ban) {
        val moderator = guild.kord.getUser(Snowflake(ban.moderator))
        log(
            guild,
            "**Info ::** User ${user.mention} :: ${user.tag} banned by ${moderator?.username} for reason: ${ban.reason}"
        )
        logger.info { buildGuildLogMessage(guild, "${user.idDescriptor()} banned by ${moderator?.idDescriptor()}") }
    }

    suspend fun userUnbanned(guild: Guild, user: User) {
        log(guild, "**Info ::** User ${user.mention} :: ${user.tag} unbanned")
        logger.info { buildGuildLogMessage(guild, "${user.idDescriptor()} unbanned") }
    }


    suspend fun infractionApplied(guild: Guild, user: User, infraction: Infraction) {
        val moderator = guild.kord.getUser(Snowflake(infraction.moderator))
        log(
            guild,
            "**Info ::** User ${user.mention} :: ${user.tag} was infracted (**${infraction.type}**) by **${moderator?.username} :: ${moderator?.tag}** \n**Reason**: ${infraction.reason}"
        )
        logger.info { buildGuildLogMessage(guild, "${user.idDescriptor()} infracted (${infraction.type}) by ${moderator?.idDescriptor()}") }
    }

    suspend fun badBfpApplied(guild: Guild, user: Member) {
        log(guild, "**Info ::** User ${user.mention} badPfp triggered for avatar <${user.pfpUrl}>")
        logger.info { buildGuildLogMessage(guild, "BadPfP triggered for ${user.idDescriptor()} with ${user.pfpUrl}") }
    }

    suspend fun badPfpCancelled(guild: Guild, user: Member) {
        log(guild, "**Info ::** BadPfp cancelled for user ${user.mention}")
        logger.info { buildGuildLogMessage(guild, "BadPfP cancelled for ${user.idDescriptor()}") }
    }

    suspend fun badPfpBan(guild: Guild, user: Member) {
        log(guild, "**Info ::** User ${user.mention} banned for not changing their avatar")
        logger.info { buildGuildLogMessage(guild, "User ${user.idDescriptor()} banned for not changing their avatar (${user.pfpUrl})") }
    }

    suspend fun initialiseMutes(guild: Guild, role: Role) {
        log(guild, "**Info ::** Existing mute timers initialized using ${role.mention} :: ${role.id.value}")
        logger.info { buildGuildLogMessage(guild, "Existing mute timers initialized using ${role.name} (${role.id})") }
    }

    suspend fun dmDisabled(guild: Guild, target: User) =
        log(
            guild,
            "**Info ::** Attempted to send direct message to ${target.mention} :: ${target.id.value} but they have DMs disabled"
        )

    suspend fun gagApplied(guild: Guild, target: Member, moderator: User) {
        log(
            guild,
            "**Info ::** User ${target.mention} has been gagged by **${moderator.username} :: ${moderator.tag}**"
        )
        logger.info { buildGuildLogMessage(guild, "${target.idDescriptor()} gagged by ${moderator.idDescriptor()}") }
    }


    suspend fun staffReactionUsed(guild: Guild, moderator: User, target: Member, reaction: ReactionEmoji) {
        log(guild, "**Info ::** ${reaction.name} used by ${moderator.username} on ${target.mention}")
        logger.info { buildGuildLogMessage(guild, "Reaction ${reaction.name} used by ${moderator.idDescriptor()}") }
    }

    suspend fun deleteReactionUsed(
        guild: Guild,
        moderator: User,
        target: Member,
        reaction: ReactionEmoji,
        message: Message
    ): List<Message?> {
        logger.info { buildGuildLogMessage(guild, "Reaction ${reaction.name} used by ${moderator.idDescriptor()}") }
        val msg = message.content.chunked(1800)

        if (msg.isNotEmpty()) {
            val firstMessage = logAndReturnMessage(
                guild,
                "**Info ::** ${reaction.name} used by ${moderator.username} on ${target.mention}\n" +
                        "**Message:**```\n" +
                        "${msg.first()}\n```"
            )

            val rest = msg.takeLast(msg.size - 1).map {
                logAndReturnMessage(guild, "**Continued:**```\n$it\n```")
            }

            return listOf(firstMessage).plus(rest)
        } else if (message.attachments.isNotEmpty()) {
            return listOf(
                logAndReturnMessage(
                    guild,
                    "**Info ::** ${reaction.name} used by ${moderator.username} on ${target.mention}\n" +
                            "**Message: (message was attachment, so only filename is logged)**```\n" +
                            "${message.attachments.first().filename}\n```"
                )
            )
        }
        return emptyList()
    }

    suspend fun pointDecayApplied(
        guild: Guild,
        target: GuildMember,
        newPoints: Int,
        pointsDeducted: Int,
        weeksSinceLastInfraction: Int
    ) {
        val user = guild.kord.getUser(Snowflake(target.userId))

        log(
            guild,
            "**Info ::** Infraction point decay for ${user?.descriptor()} " +
                    "\nUser's points are now **$newPoints** after **$weeksSinceLastInfraction** infraction free weeks. " +
                    "Previous points were ${newPoints + pointsDeducted}"
        )
        logger.info { buildGuildLogMessage(guild, "Point decay applied to ${user?.idDescriptor()}. Points reduced from ${newPoints + pointsDeducted} to $newPoints for $weeksSinceLastInfraction weeks of decay") }
    }

    private suspend fun log(guild: Guild, message: String) {
        getLoggingChannel(guild)?.createMessage(message)
    }

    private suspend fun logAndReturnMessage(guild: Guild, message: String): Message? {
        return getLoggingChannel(guild)?.createMessage(message)
    }

    private suspend fun getLoggingChannel(guild: Guild): TextChannel? {
        val channelId = configuration[guild.id]?.loggingConfiguration?.loggingChannel ?: return null
        return guild.getChannelOf(channelId)
    }

    private fun buildGuildLogMessage(guild: Guild, message: String) = "${guild.name} (${guild.id}): $message"
}
