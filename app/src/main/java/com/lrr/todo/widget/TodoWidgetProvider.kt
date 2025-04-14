package com.lrr.todo.widget

import android.app.Application
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.lrr.todo.MainActivity
import com.lrr.todo.R
import com.lrr.todo.util.SharedPreferenceUtils
import com.lrr.todo.listener.GetTodoDataCallBack
import com.lrr.todo.viewmodel.TodoViewModel
import java.util.Objects

abstract class TodoWidgetProvider : AppWidgetProvider() {

    companion object {
        const val TAG = "TodoWidgetProvider"
        const val ACTION_CLICK_TO_OPEN_GROUP_LIST = "com.lrr.todo.action.widget.open.group"
        const val ACTION_CLICK_TO_CHANGE_GROUP = "com.lrr.todo.action.widget.change.group"
        const val ACTION_CLICK_TO_OPEN_MAIN = "com.lrr.todo.action.widget.open.main"
        const val EXTRA_WIDGET_ID = "widgetId"
        const val EXTRA_GROUP_NAME = "groupName"
        const val EXTRA_GROUP_LIST = "todoList"

        fun updateAllWidget(context: Context) {
            val appWidgetManager = context.getSystemService(Context.APPWIDGET_SERVICE) as AppWidgetManager
            val smallWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, TodoSmallWidget::class.java)
            )
            TodoSmallWidget().onUpdate(context, appWidgetManager, smallWidgetIds)
            val middleWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, TodoMiddleWidget::class.java
                )
            )
            TodoMiddleWidget().onUpdate(context, appWidgetManager, middleWidgetIds)
            val largeWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, TodoLargeWidget::class.java
                )
            )
            TodoLargeWidget().onUpdate(context, appWidgetManager, largeWidgetIds)
        }
    }

    abstract fun createInstance(
        context: Context,
        widgetId: Int,
        mapData: Map<String, List<String>>
    ): RemoteViews

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        Log.d(TAG, "onReceive: intent:${intent?.action}")
        if (intent == null || context == null) {
            return
        }
        val appWidgetManager =
            context.getSystemService(Context.APPWIDGET_SERVICE) as AppWidgetManager
        val todoViewModel = TodoViewModel(context.applicationContext as Application)
        if (Objects.equals(intent.action, ACTION_CLICK_TO_OPEN_GROUP_LIST)) {
            Log.d(TAG, "onReceive: ACTION_CLICK_TO_OPEN_GROUP_LIST intent:$intent")
            val widgetId =
                intent.getIntExtra(EXTRA_WIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            todoViewModel.getWidgetDataMap(object : GetTodoDataCallBack {
                override fun onSuccess(mapData: Map<String, List<String>>) {
                    val remoteView = createInstance(context, widgetId, mapData)
                    remoteView.setViewVisibility(R.id.groupListView, View.VISIBLE)
                    appWidgetManager.updateAppWidget(widgetId, remoteView)
                }
            })
        } else if (Objects.equals(intent.action, ACTION_CLICK_TO_CHANGE_GROUP)) {
            if (intent.extras == null) {
                return
            }
            val widgetId =
                intent.extras!!.getInt(EXTRA_WIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            val groupName = intent.extras!!.getString(EXTRA_GROUP_NAME)
            if (!groupName.isNullOrEmpty()) {
                Log.d(TAG, "onReceive: saveWidgetData groupName:$groupName")
                SharedPreferenceUtils.saveWidgetData(context, groupName)
            }
            Log.d(
                TAG,
                "onReceive: ACTION_CLICK_TO_CHANGE_GROUP intent:$intent groupName:$groupName"
            )
            updateAllWidget(context)
            todoViewModel.getWidgetDataMap(object : GetTodoDataCallBack {
                override fun onSuccess(mapData: Map<String, List<String>>) {
                    val remoteView = createInstance(context, widgetId, mapData)
                    remoteView.setViewVisibility(R.id.groupListView, View.GONE)
                    appWidgetManager.updateAppWidget(widgetId, remoteView)
                }
            })
        } else if (Objects.equals(intent.action, ACTION_CLICK_TO_OPEN_MAIN)) {
            val groupName = intent.getStringExtra(MainActivity.EXTRA_CURRENT_GROUP_NAME)
            val widgetId = intent.getIntExtra(
                MainActivity.EXTRA_WIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
            val widgetSize =
                intent.getIntExtra(MainActivity.EXTRA_WIDGET_SIZE, MainActivity.WIDGET_SIZE_SMALL)
            val mainIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra(
                    MainActivity.EXTRA_CURRENT_GROUP_NAME,
                    groupName
                )
                putExtra(MainActivity.EXTRA_WIDGET_ID, widgetId)
                putExtra(MainActivity.EXTRA_WIDGET_SIZE, widgetSize)
            }
            Log.d(TAG, "onReceive: ACTION_CLICK_TO_OPEN_MAIN: groupName:$groupName")
            context.startActivity(mainIntent)
        }
    }

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        if (context == null) {
            return
        }
        appWidgetIds?.forEach {
            val todoViewModel = TodoViewModel(context.applicationContext as Application)
            todoViewModel.getWidgetDataMap(object : GetTodoDataCallBack {
                override fun onSuccess(mapData: Map<String, List<String>>) {
                    val remoteView = createInstance(context, it, mapData)
                    appWidgetManager?.updateAppWidget(it, remoteView)
                }
            })
        }
    }
}