package com.lrr.todo.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.lrr.todo.R
import com.lrr.todo.util.AlarmUtils.EXTRA_ALARM_ID
import com.lrr.todo.util.NotificationUtils
import com.lrr.todo.util.SharedPreferenceUtils

/**
 * 闹钟提醒广播
 */
class AlarmReceiver : BroadcastReceiver() {
    companion object {
        const val TAG = "AlarmReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || context == null) {
            return
        }
        val id = intent.getIntExtra(EXTRA_ALARM_ID, 0)
        val tips = SharedPreferenceUtils.getAlarmTips(context, id)
        if (tips != null) {
            NotificationUtils.notify(context,  tips)
        }
        SharedPreferenceUtils.removeAlarmData(context, id)
        Log.d(TAG, "AlarmReceiver onReceive: intent:$intent id:$id tips:$tips")
    }
}