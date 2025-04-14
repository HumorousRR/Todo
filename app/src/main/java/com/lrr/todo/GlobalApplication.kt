package com.lrr.todo

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.lrr.todo.service.TodoTipsService

class GlobalApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        mContext = this
        val channel = NotificationChannel(
            TodoTipsService.TAG,
            "Todo Tips Service",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    companion object {

        @SuppressLint("StaticFieldLeak")
        private lateinit var mContext: Context

        fun getContextInstance(): Context {
            return mContext
        }
    }

}