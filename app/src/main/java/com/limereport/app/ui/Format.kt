package com.limereport.app.ui

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val zone get() = ZoneId.systemDefault()
private val dayFmt = DateTimeFormatter.ofPattern("EEE d MMM", Locale.getDefault())
private val timeFmt = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
private val shortDayFmt = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())

fun shekels(amount: Int) = "₪$amount"

fun formatDay(millis: Long): String = Instant.ofEpochMilli(millis).atZone(zone).format(dayFmt)

fun formatTime(millis: Long): String = Instant.ofEpochMilli(millis).atZone(zone).format(timeFmt)

/** "Sat 24 Oct 18:00 → Sun 25 Oct 06:30" */
fun formatShiftRange(start: Long, end: Long): String =
    "${formatDay(start)} ${formatTime(start)} → ${formatDay(end)} ${formatTime(end)}"

/** "Mon 19 Oct – Sun 25 Oct" */
fun formatWeek(monday: LocalDate): String =
    "${monday.format(dayFmt)} – ${monday.plusDays(6).format(dayFmt)}"

fun formatWeekShort(monday: LocalDate): String =
    "${monday.format(shortDayFmt)} – ${monday.plusDays(6).format(shortDayFmt)}"
