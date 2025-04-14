package com.lrr.todo.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.lrr.todo.receiver.AlarmReceiver


/**
 * @author Lrr
 * 闹钟工具类
 */
object AlarmUtils {
    const val TAG = "AlarmUtils"
    const val EXTRA_ALARM_ID = "extraTip"

    fun setAlarm(context: Context, date: String, tip: String, id: Int) {
        if (date.isEmpty()) {
            return
        }
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
            intent.putExtra(EXTRA_ALARM_ID, id)
            PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_MUTABLE)
        }
        var tipMillis = DateUtils.convertDateStringToMillis(date)
        val cur = System.currentTimeMillis()
        if (tipMillis - cur < 0) {
            return
        }
        tipMillis -= 640000
        if (tipMillis < 0) {
            alarm.set(AlarmManager.RTC_WAKEUP, cur + 1000, alarmIntent)
        } else {
            alarm.set(AlarmManager.RTC_WAKEUP, tipMillis, alarmIntent)
        }
        val time = date.split(" ")[1]
        if (time[0] == '0') {
            time.subSequence(1, time.length)
        }
        SharedPreferenceUtils.saveAlarmData(context, id, "$time $tip")
        Log.d(
            TAG,
            "setAlarm: tipMillis:$tipMillis current:$cur 差值:${tipMillis - cur} tips:${time + tip}"
        )
    }

    fun removeAlarm(context: Context, id: Int) {
        Log.d(TAG, "removeAlarm: id:$id")
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
            intent.putExtra(EXTRA_ALARM_ID, id)
            PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_MUTABLE)
        }
        SharedPreferenceUtils.removeAlarmData(context, id)
        alarm.cancel(alarmIntent)
    }
}