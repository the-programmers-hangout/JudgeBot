package me.ddivad.judgebot.util

import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.Hours
import org.joda.time.Minutes
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import java.util.concurrent.TimeUnit

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

fun timeBetween(endTime: DateTime): String {
    val now = DateTime()
    val days = Days.daysBetween(now, endTime).days
    val hours = Hours.hoursBetween(now, endTime).hours
    val minutes = Minutes.minutesBetween(now, endTime).minutes

    return when {
        days > 0 -> "$days day(s)"
        hours > 0 -> "$hours hour(s)"
        else -> "$minutes minute(s)"
    }
}

fun formatOffsetTime(time: Instant): String {
    val days = TimeUnit.MILLISECONDS.toDays(DateTime.now().millis - time.toEpochMilli())
    val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(Locale.UK).withZone(ZoneOffset.UTC)
    return if (days > 4) {
        "${formatter.format(time)}\n($days days ago)"
    } else {
        val hours = TimeUnit.MILLISECONDS.toHours(DateTime.now().millis - time.toEpochMilli())
        "${formatter.format(time)}\n($hours hours ago)"
    }
}