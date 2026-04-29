package com.example.budgie_budgetapp.utils

import java.util.*

object DateRangeHelper {

    fun getDateRange(filter: String): Pair<Date, Date> {
        val calendar = Calendar.getInstance()

        val startDate = when (filter) {
            "Day" -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.time
            }
            "Week" -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.time
            }
            "Month" -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.time
            }
            else -> calendar.time
        }

        val endCalendar = Calendar.getInstance()
        endCalendar.set(Calendar.HOUR_OF_DAY, 23)
        endCalendar.set(Calendar.MINUTE, 59)
        endCalendar.set(Calendar.SECOND, 59)

        return Pair(startDate, endCalendar.time)
    }

    fun formatDateRange(startDate: Date, endDate: Date): String {
        val format = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
        return "${format.format(startDate)} - ${format.format(endDate)}"
    }
}
