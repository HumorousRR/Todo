package com.lrr.todo.service

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.lrr.todo.R

class TodoTipsService : Service() {

    companion object {
        const val TAG = "TodoTipsService"
        const val NOTIFICATION_ID = 1
        const val EXTRA_TIP = "tip"
    }

    private lateinit var mWindowManager: WindowManager
    private lateinit var mFloatingView: View
    private lateinit var mLayoutParams: WindowManager.LayoutParams

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        mFloatingView = LayoutInflater.from(this).inflate(R.layout.floating_view_todo_tips, null)
        mWindowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        mFloatingView.findViewById<ImageView>(R.id.closeIv).setOnClickListener {
            removeFloatingView()
        }
    }

    private fun setForegroundNotification() {
        // 创建并显示前台通知
        val notification = NotificationCompat.Builder(this, TAG)
            .setContentTitle("Todo Tips")
            .setSmallIcon(R.drawable.ic_app)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: intent:$intent extra:${intent?.extras}")
        if (intent != null) {
            // 设置前台服务通知
            setForegroundNotification()
            val tip = intent.getStringExtra(EXTRA_TIP)
            Log.d(TAG, "onStartCommand: tip:$tip")
            mFloatingView.findViewById<TextView>(R.id.tipsContentTv).text = tip
            setFloatingView()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun setFloatingView() {
        mLayoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        mLayoutParams.gravity = Gravity.TOP or Gravity.END
        mLayoutParams.x = 0
        mLayoutParams.y = resources.getDimension(R.dimen.floating_tips_y).toInt()
        if (mFloatingView.parent == null) {
            mWindowManager.addView(mFloatingView, mLayoutParams)
        } else {
            mWindowManager.removeView(mFloatingView)
            mWindowManager.addView(mFloatingView, mLayoutParams)
        }
    }

    private fun removeFloatingView() {
        if (this::mFloatingView.isInitialized && mFloatingView.parent != null) {
            mWindowManager.removeView(mFloatingView)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        removeFloatingView()
    }
}
