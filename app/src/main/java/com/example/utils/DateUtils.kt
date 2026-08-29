package com.example.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displayFormat = SimpleDateFormat("EEEE, MMMM d", Locale.US)

    fun getTodayString(): String {
        return dateFormat.format(Date())
    }

    fun getDisplayDateString(): String {
        return displayFormat.format(Date())
    }
    
    fun getYesterdayString(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        return dateFormat.format(calendar.time)
    }

    fun getSeedFromDate(dateStr: String): Long {
        return try {
            val date = dateFormat.parse(dateStr)
            date?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
    
    fun formatTime(seconds: Long): String {
        val m = seconds / 60
        val s = seconds % 60
        return String.format(Locale.US, "%02d:%02d", m, s)
    }
}
