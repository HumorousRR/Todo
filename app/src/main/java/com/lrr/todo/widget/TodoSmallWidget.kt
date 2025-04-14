package com.lrr.todo.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import androidx.core.widget.RemoteViewsCompat
import com.lrr.todo.MainActivity
import com.lrr.todo.R
import com.lrr.todo.util.SharedPreferenceUtils

class TodoSmallWidget : TodoWidgetProvider() {

    override fun createInstance(
        context: Context,
        widgetId: Int,
        mapData: Map<String, List<String>>
    ): RemoteViews {
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_small)
        //设置事件
        var curGroupName = SharedPreferenceUtils.getWidgetData(context)

        val contentList = if (mapData.containsKey(curGroupName)) {
            remoteViews.setTextViewText(R.id.groupNameTv, curGroupName)
            mapData[curGroupName]
        } else if (mapData.keys.isNotEmpty()) {
            curGroupName = mapData.keys.first()
            remoteViews.setTextViewText(R.id.groupNameTv, curGroupName)
            mapData.values.first()
        } else {
            null
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(
                MainActivity.EXTRA_CURRENT_GROUP_NAME,
                curGroupName
            )
            putExtra(MainActivity.EXTRA_WIDGET_ID, widgetId)
            putExtra(MainActivity.EXTRA_WIDGET_SIZE, MainActivity.WIDGET_SIZE_SMALL)
        }
        val appOpenIntent = PendingIntent.getActivity(
            context,
            widgetId,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        remoteViews.setOnClickPendingIntent(R.id.rootRl, appOpenIntent)
        val listIntent = Intent(context, TodoSmallWidget::class.java).apply {
            action = ACTION_CLICK_TO_OPEN_MAIN
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(
                MainActivity.EXTRA_CURRENT_GROUP_NAME,
                curGroupName
            )
            putExtra(MainActivity.EXTRA_WIDGET_ID, widgetId)
            putExtra(MainActivity.EXTRA_WIDGET_SIZE, MainActivity.WIDGET_SIZE_SMALL)
        }
        remoteViews.setPendingIntentTemplate( //设置ListView中Item临时占位Intent
            R.id.listView, PendingIntent.getBroadcast(
                context,
                0,
                listIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )

        //设置显示信息
        if (contentList.isNullOrEmpty()) {
            remoteViews.setViewVisibility(R.id.noneTodoLl, View.VISIBLE)
            remoteViews.setViewVisibility(R.id.listView, View.GONE)
        } else {
            remoteViews.setViewVisibility(R.id.noneTodoLl, View.GONE)
            remoteViews.setViewVisibility(R.id.listView, View.VISIBLE)
            RemoteViewsCompat.setRemoteAdapter(
                context = context,
                remoteViews = remoteViews,
                appWidgetId = widgetId,
                viewId = R.id.listView,
                items = getRemoteItems(context, contentList)
            )
        }
        return remoteViews
    }

    private fun getRemoteItems(
        context: Context,
        contentList: List<String>,
    ): RemoteViewsCompat.RemoteCollectionItems {
        val builder = RemoteViewsCompat.RemoteCollectionItems.Builder()
        contentList.forEachIndexed { index, content ->
            val remoteViews = RemoteViews(context.packageName, R.layout.widget_todo_item_small)
            val clickIntent = Intent(context, TodoSmallWidget::class.java).apply {
                action = ACTION_CLICK_TO_OPEN_MAIN
            }
            remoteViews.setOnClickFillInIntent(R.id.rootLl, clickIntent)
            remoteViews.setTextViewText(R.id.contentTv, content)
            builder.addItem(index.toLong(), remoteViews)
        }
        return builder.setHasStableIds(true).setViewTypeCount(contentList.count()).build()
    }
}