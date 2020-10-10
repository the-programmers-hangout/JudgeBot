package me.ddivad.judgebot.dataclasses

data class Punishment(val userId: String,
                      val type: InfractionType,
                      var reason: String,
                      val clearTime: Long,
                      var id: Int = 0)