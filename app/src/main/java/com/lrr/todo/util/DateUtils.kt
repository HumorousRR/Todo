package com.lrr.todo.util

import android.annotation.SuppressLint
import android.util.Log
import com.loper7.date_time_picker.DateTimeConfig
import com.loper7.date_time_picker.DateTimePicker
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Date

object DateUtils {

    const val TAG = "DateUtils"

    /**
     * 获取日期
     * @param DateTimePicker
     * @return yyyy-MM-dd HH:MM格式日期
     */
    fun getDateString(picker: DateTimePicker): String {
        val date = StringBuilder()
        var month = picker.getPicker(DateTimeConfig.MONTH)?.value.toString()
        if (month.length == 1) {
            month = "0$month"
        }
        var day = picker.getPicker(DateTimeConfig.DAY)?.value.toString()
        if (day.length == 1) {
            day = "0$day"
        }
        var hour = picker.getPicker(DateTimeConfig.HOUR)?.value.toString()
        if (hour.length == 1) {
            hour = "0$hour"
        }
        var min = picker.getPicker(DateTimeConfig.MIN)?.value.toString()
        if (min.length == 1) {
            min = "0$min"
        }
        date.append(picker.getPicker(DateTimeConfig.YEAR)?.value).append('-')
            .append(month).append('-')
            .append(day).append(" ")
            .append(hour).append(":")
            .append(min)
        return date.toString()
    }

    @SuppressLint("SimpleDateFormat")
    fun setDateTimePicker(picker: DateTimePicker) {
        val currentDate = LocalDateTime.now()
        val year = currentDate.year
        val month = currentDate.month
        val day = currentDate.dayOfMonth
        val hour = currentDate.hour
        val min = currentDate.minute
    }

    /**
     * 时间是否已过期
     */
    @SuppressLint("SimpleDateFormat")
    fun hasReachedTime(date: String): Boolean {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
        try {
            val targetDate: Date? = dateFormat.parse(date)
            val currentDate = Date()
            return targetDate?.compareTo(currentDate)!! < 0
        } catch (e: Exception) {
            Log.e(TAG, "hasReachedTime: $e")
        }
        return false
    }

    /**
     * 将yyyy-MM-dd HH:MM格式日期转换为mills
     */
    @SuppressLint("SimpleDateFormat")
    fun convertDateStringToMillis(dateString: String): Long {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
        val date = dateFormat.parse(dateString)
        if (date != null) {
            return date.time
        }
        return 0
    }
}