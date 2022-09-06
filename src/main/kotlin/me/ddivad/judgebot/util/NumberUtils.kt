package me.ddivad.judgebot.util

fun timeToString(milliseconds: Long): String {
    val seconds = (milliseconds / 1000) % 60
    val minutes = (milliseconds / (1000 * 60)) % 60
    val hours = (milliseconds / (1000 * 60 * 60)) % 24
    val days = (milliseconds / (1000 * 60 * 60 * 24))
    val dayString = if (days > 0) "$days day(s) " else ""
    val hourString = if (hours > 0) "$hours hour(s) " else ""
    val minuteString = if (minutes > 0) "$minutes minute(s) " else ""
    val secondString = if (seconds > 0) "$seconds second(s)" else ""
    return ("$dayString$hourString$minuteString$secondString")
}