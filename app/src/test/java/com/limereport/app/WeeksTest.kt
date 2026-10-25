package com.limereport.app

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class WeeksTest {
    private val zone = ZoneId.of("Asia/Jerusalem")
    private fun millis(s: String) = LocalDateTime.parse(s).atZone(zone).toInstant().toEpochMilli()

    @Test fun mondayMidnightStartsNewWeek() {
        assertEquals(LocalDate.parse("2026-10-26"), Weeks.weekStartOf(millis("2026-10-26T00:00"), zone))
        assertEquals(LocalDate.parse("2026-10-19"), Weeks.weekStartOf(millis("2026-10-25T23:59"), zone))
    }

    @Test fun sundayNightShiftCountsInWeekItStarted() {
        assertEquals(LocalDate.parse("2026-10-19"), Weeks.weekStartOf(millis("2026-10-25T19:00"), zone))
    }
}
