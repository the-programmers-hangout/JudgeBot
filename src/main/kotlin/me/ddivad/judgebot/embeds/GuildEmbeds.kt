package me.ddivad.judgebot.embeds

import dev.kord.common.entity.Snowflake
import dev.kord.common.kColor
import dev.kord.core.entity.Guild
import dev.kord.rest.Image
import dev.kord.rest.builder.message.EmbedBuilder
import me.ddivad.judgebot.dataclasses.GuildConfiguration
import me.ddivad.judgebot.dataclasses.Punishment
import me.ddivad.judgebot.util.timeToString
import me.jakejmattson.discordkt.extensions.TimeStamp
import me.jakejmattson.discordkt.extensions.TimeStyle
import me.jakejmattson.discordkt.extensions.addField
import java.awt.Color
import java.time.Instant

suspend fun EmbedBuilder.createConfigEmbed(config: GuildConfiguration, guild: Guild) {
    title = "Configuration"
    color = Color.MAGENTA.kColor
    thumbnail {
        url = guild.getIconUrl(Image.Format.PNG) ?: ""
    }

    field {
        name = "**General:**"
        value = "Bot Prefix: ${config.prefix} \n" +
                "Admin Roles: ${config.adminRoles.map { guild.getRoleOrNull(it)?.mention }} \n" +
                "Staff Roles: ${config.staffRoles.map { guild.getRoleOrNull(it)?.mention }} \n" +
                "Moderator Roles: ${config.moderatorRoles.map { guild.getRoleOrNull(it)?.mention }} \n" +
                "Mute Role: ${guild.getRoleOrNull(config.mutedRole)?.mention} \n"
    }

    field {
        name = "**Infractions:**"
        value = "Point Ceiling: ${config.infractionConfiguration.pointCeiling} \n" +
                "Strike points: ${config.infractionConfiguration.strikePoints} \n" +
                "Warn Points: ${config.infractionConfiguration.warnPoints} \n" +
                "Warn Upgrade Prompt: ${config.infractionConfiguration.warnUpgradeThreshold} Points\n" +
                "Point Decay / Week: ${config.infractionConfiguration.pointDecayPerWeek} \n" +
                "Gag Duration: ${timeToString(config.infractionConfiguration.gagDuration)}"
    }

    field {
        name = "**Reactions**"
        value = "Enabled: ${config.reactions.enabled} \n" +
                "Gag: ${config.reactions.gagReaction} \n" +
                "Delete Message: ${config.reactions.deleteMessageReaction} \n" +
                "Flag Message: ${config.reactions.flagMessageReaction}"
    }

    field {
        name = "**Punishments:**"
        config.punishments.forEach {
            value += "Punishment Type: ${it.punishment} \n" +
                    "Point Threshold: ${it.points} \n" +
                    "Punishment Duration: ${if (it.duration !== null) timeToString(it.duration!!) else "Permanent"}" + "\n\n"
        }
    }

    field {
        name = "**Logging:**"
        value = "Logging Channel: ${guild.getChannelOrNull(config.loggingConfiguration.loggingChannel)?.mention} \n" +
                "Alert Channel: ${guild.getChannelOrNull(config.loggingConfiguration.alertChannel)?.mention}"
    }
    footer {
        icon = guild.getIconUrl(Image.Format.PNG) ?: ""
        text = guild.name
    }
}

suspend fun EmbedBuilder.createActivePunishmentsEmbed(guild: Guild, punishments: List<Punishment>) {
    title = "__Active Punishments__"
    color = Color.MAGENTA.kColor
    punishments.forEach {
        val user = guild.kord.getUser(Snowflake(it.userId))?.mention
        addField(
            "${it.id} - ${it.type} - ${
                if (it.clearTime != null) "Cleartime - ${
                    TimeStamp.at(
                        Instant.ofEpochMilli(it.clearTime),
                        TimeStyle.RELATIVE
                    )
                }" else ""
            }",
            "User: $user"
        )
    }
    footer {
        icon = guild.getIconUrl(Image.Format.PNG) ?: ""
        text = guild.name
    }
}
