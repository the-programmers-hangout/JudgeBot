package me.ddivad.judgebot.dataclasses

enum class PunishmentType {
    Warn, Mute, BadPfp, Strike
}

data class Punishment(val userId: String,
                      val guildId: String,
                      val type: PunishmentType,
                      var reason: String,
                      val clearTime: Long)