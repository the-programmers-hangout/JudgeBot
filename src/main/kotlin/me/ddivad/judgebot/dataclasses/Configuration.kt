package me.ddivad.judgebot.dataclasses

import dev.kord.core.entity.Guild
import dev.kord.core.entity.Role
import me.jakejmattson.discordkt.api.dsl.Data

data class Configuration(
    val ownerId: String = "insert-owner-id",
    var prefix: String = "judge!",
    val guildConfigurations: MutableMap<Long, GuildConfiguration> = mutableMapOf(),
    val dbConfiguration: DatabaseConfiguration = DatabaseConfiguration()
) : Data("config/config.json") {
    operator fun get(id: Long) = guildConfigurations[id]
    fun hasGuildConfig(guildId: Long) = guildConfigurations.containsKey(guildId)

    fun setup(
        guild: Guild,
        prefix: String,
        adminRole: Role,
        staffRole: Role,
        moderatorRole: Role,
        mutedRole: Role,
        logging: LoggingConfiguration
    ) {
        if (guildConfigurations[guild.id.value] != null) return

        val newConfiguration = GuildConfiguration(
            guild.id.asString,
            prefix,
            mutableListOf(moderatorRole.id.asString),
            mutableListOf(staffRole.id.asString),
            mutableListOf(adminRole.id.asString),
            mutedRole.id.asString,
            logging
        )

        // Setup default punishments
        // TODO: Add configuration commands for this
        newConfiguration.punishments.add(PunishmentLevel(0, PunishmentType.NONE, 0L))
        newConfiguration.punishments.add(PunishmentLevel(10, PunishmentType.MUTE, 1000L * 60 * 60 * 1))
        newConfiguration.punishments.add(PunishmentLevel(20, PunishmentType.MUTE, 1000L * 60 * 60 * 12))
        newConfiguration.punishments.add(PunishmentLevel(30, PunishmentType.MUTE, 1000L * 60 * 60 * 24))
        newConfiguration.punishments.add(PunishmentLevel(40, PunishmentType.MUTE, 1000L * 60 * 60 * 24 * 30))
        newConfiguration.punishments.add(PunishmentLevel(50, PunishmentType.BAN))

        guildConfigurations[guild.id.value] = newConfiguration
        save()
    }
}

data class DatabaseConfiguration(
    val address: String = "mongodb://localhost:27017",
    val databaseName: String = "judgebot"
)

data class GuildConfiguration(
    val id: String = "",
    var prefix: String = "j!",
    var moderatorRoles: MutableList<String> = mutableListOf(),
    var staffRoles: MutableList<String> = mutableListOf(),
    var adminRoles: MutableList<String> = mutableListOf(),
    var mutedRole: String = "",
    var loggingConfiguration: LoggingConfiguration = LoggingConfiguration(),
    var infractionConfiguration: InfractionConfiguration = InfractionConfiguration(),
    var punishments: MutableList<PunishmentLevel> = mutableListOf(),
    var reactions: ReactionConfiguration = ReactionConfiguration()
)

data class LoggingConfiguration(
    var alertChannel: String = "",
    var loggingChannel: String = "insert_id",
    var logRoles: Boolean = true,
    var logInfractions: Boolean = true,
    var logPunishments: Boolean = true
)

data class InfractionConfiguration(
    var pointCeiling: Int = 50,
    var strikePoints: Int = 10,
    var warnPoints: Int = 0,
    var pointDecayPerWeek: Int = 2,
)

data class PunishmentLevel(
    var points: Int = 0,
    var punishment: PunishmentType,
    var duration: Long? = null
)

data class ReactionConfiguration(
    var enabled: Boolean = true,
    var gagReaction: String = "",
    var historyReaction: String = "",
    var deleteMessageReaction: String = "",
    var flagMessageReaction: String = ""
)