package me.ddivad.judgebot.dataclasses

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Guild
import dev.kord.x.emoji.Emojis
import kotlinx.serialization.Serializable
import me.jakejmattson.discordkt.dsl.Data
import me.jakejmattson.discordkt.dsl.edit
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Serializable
data class Configuration(
    val guildConfigurations: MutableMap<Snowflake, GuildConfiguration> = mutableMapOf(),
    val dbConfiguration: DatabaseConfiguration = DatabaseConfiguration()
) : Data() {
    operator fun get(id: Snowflake) = guildConfigurations[id]
    fun setup(
        guild: Guild, configuration: GuildConfiguration
    ) {
        if (guildConfigurations[guild.id] != null) return

        // Setup default punishments
        // TODO: Add configuration commands for this
        configuration.punishments.add(PunishmentLevel(0, PunishmentType.NONE, 0L))
        configuration.punishments.add(
            PunishmentLevel(
                10, PunishmentType.MUTE, 1.toDuration(DurationUnit.HOURS).inWholeMilliseconds
            )
        )
        configuration.punishments.add(
            PunishmentLevel(
                20, PunishmentType.MUTE, 12.toDuration(DurationUnit.HOURS).inWholeMilliseconds
            )
        )
        configuration.punishments.add(
            PunishmentLevel(
                30, PunishmentType.MUTE, 24.toDuration(DurationUnit.HOURS).inWholeMilliseconds
            )
        )
        configuration.punishments.add(
            PunishmentLevel(
                40, PunishmentType.MUTE, 28.toDuration(DurationUnit.DAYS).inWholeMilliseconds
            )
        )
        configuration.punishments.add(PunishmentLevel(50, PunishmentType.BAN))

        edit { guildConfigurations[guild.id] = configuration }
    }
}

@Serializable
data class DatabaseConfiguration(
    val address: String = "mongodb://localhost:27017", val databaseName: String = "judgebot"
)

@Serializable
data class GuildConfiguration(
    var prefix: String = "j!",
    var moderatorRoles: MutableList<Snowflake> = mutableListOf(),
    var staffRoles: MutableList<Snowflake> = mutableListOf(),
    var adminRoles: MutableList<Snowflake> = mutableListOf(),
    var mutedRole: Snowflake,
    var loggingConfiguration: LoggingConfiguration,
    var infractionConfiguration: InfractionConfiguration = InfractionConfiguration(),
    var punishments: MutableList<PunishmentLevel> = mutableListOf(),
    var reactions: ReactionConfiguration = ReactionConfiguration()
)

@Serializable
data class LoggingConfiguration(
    var alertChannel: Snowflake,
    var loggingChannel: Snowflake,
)

@Serializable
data class InfractionConfiguration(
    var pointCeiling: Int = 50,
    var strikePoints: Int = 10,
    var warnPoints: Int = 0,
    var warnUpgradeThreshold: Int = 40,
    var pointDecayPerWeek: Int = 2,
    var gagDuration: Long = 5.toDuration(DurationUnit.MINUTES).inWholeMilliseconds
)

@Serializable
data class PunishmentLevel(
    var points: Int = 0, var punishment: PunishmentType, var duration: Long? = null
)

@Serializable
data class ReactionConfiguration(
    var enabled: Boolean = true,
    var gagReaction: String = "${Emojis.mute}",
    var deleteMessageReaction: String = "${Emojis.wastebasket}",
    var flagMessageReaction: String = "${Emojis.stopSign}"
)