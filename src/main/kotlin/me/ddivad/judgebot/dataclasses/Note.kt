package me.ddivad.judgebot.dataclasses

data class Note(val note: String,
                val moderator: String,
                val dateTime: Long,
                val id: Int)