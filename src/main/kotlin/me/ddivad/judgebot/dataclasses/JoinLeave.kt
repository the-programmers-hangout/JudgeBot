package me.ddivad.judgebot.dataclasses

data class JoinLeave(
    val guildId: String,
    val userId: String,
    val joinDate: Long,
    var leaveDate: Long? = null
)