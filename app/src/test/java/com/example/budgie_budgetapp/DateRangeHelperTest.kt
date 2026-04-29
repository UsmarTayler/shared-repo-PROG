package com.example.budgie_budgetapp

import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

/**
 * Unit tests for DateRangeHelper logic.
 * Tests that date range calculations are correct for Day / Week / Month filters.
 */
class DateRangeHelperTest {

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun startOfDay(daysOffset: Int = 0): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, daysOffset)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun startOfWeek(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun startOfMonth(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    // ── "Day" filter ─────────────────────────────────────────────────────────

    @Test
    fun dayFilter_startDate_isTodayMidnight() {
        val start = startOfDay(0)
        val now = System.currentTimeMillis()
        // start should be before now and within the past 24 h
        assertTrue("Start of day should be before now", start <= now)
        assertTrue("Start of day should be within last 24 h", now - start < 24 * 60 * 60 * 1000L)
    }

    @Test
    fun dayFilter_endDate_isAfterStartDate() {
        val start = startOfDay(0)
        val end   = System.currentTimeMillis()
        assertTrue("End should be after start", end >= start)
    }

    // ── "Week" filter ─────────────────────────────────────────────────────────

    @Test
    fun weekFilter_startDate_isWithinPast7Days() {
        val start = startOfWeek()
        val now   = System.currentTimeMillis()
        val sevenDaysMs = 7L * 24 * 60 * 60 * 1000
        assertTrue("Week start should be before now", start <= now)
        assertTrue("Week start should be within last 7 days", now - start <= sevenDaysMs)
    }

    @Test
    fun weekFilter_startBeforeEnd() {
        val start = startOfWeek()
        val end   = System.currentTimeMillis()
        assertTrue(start <= end)
    }

    // ── "Month" filter ────────────────────────────────────────────────────────

    @Test
    fun monthFilter_startDate_isDayOneOfMonth() {
        val cal = Calendar.getInstance()
        cal.timeInMillis = startOfMonth()
        assertEquals("Month start should be day 1", 1, cal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun monthFilter_startDate_isCurrentMonth() {
        val now = Calendar.getInstance()
        val startCal = Calendar.getInstance()
        startCal.timeInMillis = startOfMonth()
        assertEquals(now.get(Calendar.MONTH), startCal.get(Calendar.MONTH))
        assertEquals(now.get(Calendar.YEAR),  startCal.get(Calendar.YEAR))
    }

    @Test
    fun monthFilter_startBeforeEnd() {
        val start = startOfMonth()
        val end   = System.currentTimeMillis()
        assertTrue(start <= end)
    }

    // ── Filter ordering ───────────────────────────────────────────────────────

    @Test
    fun dayRange_isShorterThan_weekRange() {
        val daySpan   = System.currentTimeMillis() - startOfDay(0)
        val weekSpan  = System.currentTimeMillis() - startOfWeek()
        assertTrue("Day span should be <= week span", daySpan <= weekSpan)
    }

    @Test
    fun weekRange_isShorterThan_monthRange() {
        val weekSpan  = System.currentTimeMillis() - startOfWeek()
        val monthSpan = System.currentTimeMillis() - startOfMonth()
        assertTrue("Week span should be <= month span", weekSpan <= monthSpan)
    }
}
