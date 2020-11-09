package me.ddivad.judgebot.dataclasses

data class Punishment(val userId: String,
                      val type: InfractionType,
                      var reason: String,
                      var moderator: String,
                      val clearTime: Long? = null,
                      var id: Int = 0)

data class Ban(val userId: String,
               val moderator: String,
               var reason: String)

enum class PunishmentType {
    MUTE, BAN, NONE
}