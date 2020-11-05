package me.ddivad.judgebot.dataclasses

data class Punishment(val userId: String,
                      val type: InfractionType,
                      var reason: String,
                      var moderator: String,
                      val clearTime: Long? = null,
                      var id: Int = 0)

data class Ban(val userId: String,
               val moderator: String,
<<<<<<< HEAD
               var reason: String)

enum class PunishmentType {
    MUTE, BAN
}
=======
               val reason: String,
               val clearTime: Long? = null)

enum class PunishmentType {
    MUTE, BAN
}
>>>>>>> 63cd7656c9035e2c078eb824e971c674ac10d7f3
