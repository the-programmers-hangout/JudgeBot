package me.ddivad.judgebot.dataclasses

import java.util.*

enum class InfractionType {
    Warn, Strike
}

data class Infraction(
        val moderator: String,
        val reason: String,
        val weight: InfractionType,
        val strikes: Int = 0,
        val dateTime: Long = Date().time
)