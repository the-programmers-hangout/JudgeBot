package me.ddivad.starter.util

fun timeToString(milliseconds: Long): String{
    val seconds = (milliseconds / 1000) % 60
    val minutes = (milliseconds / (1000 * 60)) % 60
    val hours = (milliseconds / (1000 * 60 * 60)) % 24
    val days = (milliseconds / (1000 * 60 * 60 * 24))
    return ("$days days, $hours hours, $minutes minutes, $seconds seconds")
}