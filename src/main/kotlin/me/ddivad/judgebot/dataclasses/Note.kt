package me.ddivad.judgebot.dataclasses

import org.joda.time.DateTime

data class Note(val note: String,
                val moderator: String,
                val dateTime: Long,
                val id: Int)

data class Info(val message: String,
                val moderator: String,
                val dateTime: Long = DateTime().millis,
                var id: Int? = null)