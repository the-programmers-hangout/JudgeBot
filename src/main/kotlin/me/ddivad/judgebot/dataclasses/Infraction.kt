package me.ddivad.judgebot.dataclasses

import java.util.*

enum class InfractionType {
    Warn, Strike, Mute, BadPfp, Ban
}

data class Infraction(
    val moderator: String,
    val reason: String,
    val type: InfractionType,
    var points: Int = 0,
    val ruleNumber: Int? = null,
    val dateTime: Long = Date().time,
    var punishment: PunishmentLevel? = null,
    var id: Int? = null
)

data class Punishment(
    val userId: String,
    val type: InfractionType,
    val clearTime: Long? = null,
    var id: Int = 0
)

data class Ban(
    val userId: String,
    val moderator: String,
    var reason: String,
    val dateTime: Long = Date().time,
    var unbanTime: Long? = null,
    var thinIce: Boolean = false
)

enum class PunishmentType {
    MUTE, BAN, NONE
}