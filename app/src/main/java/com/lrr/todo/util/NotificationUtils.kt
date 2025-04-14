package com.lrr.todo.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.graphics.BitmapFactory
import com.lrr.todo.R


object NotificationUtils {
    private const val CHANNEL_ID = "com.lrr.todo"

    fun areNotificationsEnabled(context: Context): Boolean {
        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.areNotificationsEnabled()
    }

    fun notify(context: Context, content: String) {
        notify(context, CHANNEL_ID, content)
    }

    fun notify(context: Context, channelId: String, content: String) {
        val notification = Notification.Builder(context, channelId)
            .setContentTitle(content)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.ic_app)
            .build()
        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel =
            NotificationChannel(channelId, "Todo Notification", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)
        notificationManager.notify(1123, notification)
    }
}