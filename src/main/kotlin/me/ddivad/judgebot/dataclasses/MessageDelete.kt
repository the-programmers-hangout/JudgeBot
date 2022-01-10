package me.ddivad.judgebot.dataclasses

import java.util.Date

data class MessageDelete(
    val userId: String,
    val guildId: String,
    val messageLink: String?,
    val dateTime: Long = Date().time,
)