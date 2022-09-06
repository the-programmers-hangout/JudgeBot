package me.ddivad.judgebot.dataclasses

import java.time.Instant

data class MessageDelete(
    val userId: String,
    val guildId: String,
    val messageLink: String?,
    val dateTime: Long = Instant.now().toEpochMilli(),
)