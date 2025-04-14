package com.lrr.todo.util

import android.app.Activity
import android.util.Log
import android.view.WindowMetrics


object ScreenUtils {
    const val TAG = "ScreenUtils"

    fun getScreenHeight(activity: Activity): Int {
        val windowMetrics: WindowMetrics = activity.windowManager.currentWindowMetrics
        val height = windowMetrics.bounds.height()
        Log.d(TAG, "getScreenHeight: $height")
        return height
    }

}