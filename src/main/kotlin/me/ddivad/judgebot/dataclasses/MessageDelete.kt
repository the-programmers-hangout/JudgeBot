package me.ddivad.judgebot.dataclasses

import java.util.*

data class MessageDelete(
    val userId: String,
    val guildId: String,
    val messageLink: String?,
    val dateTime: Long = Date().time,
)