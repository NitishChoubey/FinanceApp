package com.ebf.financeapp.util



import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateFormatter {

    private val displayFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val shortFormat   = SimpleDateFormat("dd MMM", Locale.getDefault())
    private val dayFormat     = SimpleDateFormat("EEE", Locale.getDefault())   // "Mon"
    private val monthYear     = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val chartDay      = SimpleDateFormat("EEE dd", Locale.getDefault()) // "Mon 01"

    fun format(epochMillis: Long): String =
        displayFormat.format(Date(epochMillis))

    fun formatShort(epochMillis: Long): String =
        shortFormat.format(Date(epochMillis))

    fun formatDayOfWeek(epochMillis: Long): String =
        dayFormat.format(Date(epochMillis))

    fun formatMonthYear(epochMillis: Long): String =
        monthYear.format(Date(epochMillis))

    fun formatChartDay(epochMillis: Long): String =
        chartDay.format(Date(epochMillis))

    fun getCurrentMonth(): Int = Calendar.getInstance().get(Calendar.MONTH) + 1

    fun getCurrentYear(): Int = Calendar.getInstance().get(Calendar.YEAR)

    fun getCurrentYearString(): String = getCurrentYear().toString()

    // Returns epoch millis for start of a day
    fun startOfDay(epochMillis: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = epochMillis
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    // Last N days as list of (epochStart, label) pairs for chart
    fun lastNDays(n: Int): List<Pair<Long, String>> {
        val result = mutableListOf<Pair<Long, String>>()
        val cal = Calendar.getInstance()
        repeat(n) {
            val epochStart = startOfDay(cal.timeInMillis)
            result.add(0, Pair(epochStart, dayFormat.format(Date(epochStart))))
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        return result
    }

    // "Today", "Yesterday", or formatted date
    fun formatRelative(epochMillis: Long): String {
        val todayStart = startOfDay(System.currentTimeMillis())
        val dayStart   = startOfDay(epochMillis)
        return when (dayStart) {
            todayStart            -> "Today"
            todayStart - 86400000 -> "Yesterday"
            else                  -> formatShort(epochMillis)
        }
    }
}