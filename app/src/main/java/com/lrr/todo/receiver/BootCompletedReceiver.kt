package com.lrr.todo.receiver

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.lrr.todo.data.models.ToDoData
import com.lrr.todo.listener.GetAlarmDataCallBack
import com.lrr.todo.util.AlarmUtils
import com.lrr.todo.viewmodel.TodoViewModel

class BootCompletedReceiver : BroadcastReceiver() {
    companion object {
        const val TAG = "BootCompletedReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val model = TodoViewModel(context.applicationContext as Application)
            model.getAlarmData(object : GetAlarmDataCallBack {
                override fun onSuccess(data: List<ToDoData>) {
                    Log.d(TAG, "GetAlarmDataCallBack onSuccess: $data")
                    data.forEach {
                        AlarmUtils.setAlarm(context, it.remindTime, it.content, it.id)
                    }
                }
            })
        }
    }
}
