package me.ddivad.judgebot.dataclasses

import java.time.Instant

data class Note(
    var note: String,
    var moderator: String,
    val dateTime: Long,
    val id: Int
)

data class Info(
    val message: String,
    val moderator: String,
    val dateTime: Long = Instant.now().toEpochMilli(),
    var id: Int? = null
)