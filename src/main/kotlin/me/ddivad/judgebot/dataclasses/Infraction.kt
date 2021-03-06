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