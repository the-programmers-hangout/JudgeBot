package me.ddivad.judgebot.dataclasses

data class Punishment(val userId: String,
                      val type: InfractionType,
                      var reason: String,
                      val clearTime: Long,
                      var id: Int = 0)

data class Ban(val userId: String,
               val moderator: String,
               val reason: String,
               val clearTime: Long? = null)

enum class PunishmentType {
    MUTE, BAN
}