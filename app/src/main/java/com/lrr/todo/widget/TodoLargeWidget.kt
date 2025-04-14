package com.lrr.todo.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.core.widget.RemoteViewsCompat
import com.lrr.todo.MainActivity
import com.lrr.todo.R
import com.lrr.todo.util.SharedPreferenceUtils


class TodoLargeWidget : TodoWidgetProvider() {
    override fun createInstance(
        context: Context,
        widgetId: Int,
        mapData: Map<String, List<String>>
    ): RemoteViews {
        Log.d(TAG, "createInstance: large")
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_large)
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
        val groupList = if (mapData.keys.isNotEmpty()) {
            mapData.keys.toList()
        } else {
            ArrayList()
        }

        //设置打开主界面事件
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(
                MainActivity.EXTRA_CURRENT_GROUP_NAME,
                curGroupName
            )
            putExtra(MainActivity.EXTRA_WIDGET_ID, widgetId)
            putExtra(MainActivity.EXTRA_WIDGET_SIZE, MainActivity.WIDGET_SIZE_LARGE)
        }
        val appOpenIntent = PendingIntent.getActivity(
            context,
            widgetId,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        remoteViews.setOnClickPendingIntent(R.id.rootRl, appOpenIntent)
        val listIntent = Intent(context, TodoLargeWidget::class.java).apply {
            action = ACTION_CLICK_TO_OPEN_MAIN
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(
                MainActivity.EXTRA_CURRENT_GROUP_NAME,
                curGroupName
            )
            putExtra(MainActivity.EXTRA_WIDGET_ID, widgetId)
            putExtra(MainActivity.EXTRA_WIDGET_SIZE, MainActivity.WIDGET_SIZE_LARGE)
        }
        remoteViews.setPendingIntentTemplate(
            R.id.listView, PendingIntent.getBroadcast(
                context,
                0,
                listIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
        //设置打开切换Group的列表事件
        val clickGroupNameIntent = Intent(context, TodoLargeWidget::class.java).apply {
            action = ACTION_CLICK_TO_OPEN_GROUP_LIST
            putExtra(EXTRA_WIDGET_ID, widgetId)
        }
        val clickPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            clickGroupNameIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        remoteViews.setOnClickPendingIntent(R.id.groupNameLl, clickPendingIntent)

        //设置显示信息
        if (contentList.isNullOrEmpty()) {
            remoteViews.setViewVisibility(R.id.noDataTipLl, View.VISIBLE)
            remoteViews.setViewVisibility(R.id.listView, View.GONE)
            if (mapData.isEmpty()) {
                remoteViews.setViewVisibility(R.id.groupNameLl, View.GONE)
            } else {
                remoteViews.setViewVisibility(R.id.groupNameLl, View.VISIBLE)
            }
            remoteViews.setTextViewText(R.id.todoCountTv, "0")
        } else {
            remoteViews.setViewVisibility(R.id.noDataTipLl, View.GONE)
            remoteViews.setViewVisibility(R.id.listView, View.VISIBLE)
            remoteViews.setViewVisibility(R.id.groupNameLl, View.VISIBLE)
            RemoteViewsCompat.setRemoteAdapter(
                context = context,
                remoteViews = remoteViews,
                appWidgetId = widgetId,
                viewId = R.id.listView,
                items = getTodoRemoteItems(context, contentList)
            )
            remoteViews.setTextViewText(R.id.todoCountTv, contentList.size.toString())
            if (groupList.isNotEmpty()) {
                val tempIntent = Intent(context, TodoLargeWidget::class.java).apply {
                    action = ACTION_CLICK_TO_CHANGE_GROUP
                }
                remoteViews.setPendingIntentTemplate( //设置ListView中Item临时占位Intent
                    R.id.groupListView, PendingIntent.getBroadcast(
                        context,
                        0,
                        tempIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                    )
                )
                RemoteViewsCompat.setRemoteAdapter(
                    context = context,
                    remoteViews = remoteViews,
                    appWidgetId = widgetId,
                    viewId = R.id.groupListView,
                    items = getGroupRemoteItems(
                        context,
                        groupList,
                        curGroupName!!,
                        widgetId
                    )
                )
            }
        }
        return remoteViews
    }

    private fun getTodoRemoteItems(
        context: Context,
        contentList: List<String>,
    ): RemoteViewsCompat.RemoteCollectionItems {
        val builder = RemoteViewsCompat.RemoteCollectionItems.Builder()
        contentList.forEachIndexed { index, content ->
            val remoteViews = RemoteViews(context.packageName, R.layout.widget_todo_item_middle)
            val clickIntent = Intent(context, TodoLargeWidget::class.java).apply {
                action = ACTION_CLICK_TO_OPEN_MAIN
            }
            remoteViews.setOnClickFillInIntent(R.id.rootLl, clickIntent)
            remoteViews.setTextViewText(R.id.contentTv, content)
            builder.addItem(index.toLong(), remoteViews)
        }
        return builder.setHasStableIds(true).setViewTypeCount(contentList.count()).build()
    }

    private fun getGroupRemoteItems(
        context: Context,
        contentList: List<String>,
        currentGroupName: String,
        widgetId: Int,
    ): RemoteViewsCompat.RemoteCollectionItems {
        val builder = RemoteViewsCompat.RemoteCollectionItems.Builder()
        contentList.forEachIndexed { index, content ->
            val remoteViews = RemoteViews(context.packageName, R.layout.widget_group_item)
            remoteViews.setTextViewText(R.id.groupNameTv, content)
            val extra = Bundle()
            extra.putInt(EXTRA_WIDGET_ID, widgetId)
            extra.putString(EXTRA_GROUP_NAME, content)
            val clickGroupNameIntent = Intent(context, TodoLargeWidget::class.java).apply {
                action = ACTION_CLICK_TO_CHANGE_GROUP
                putExtras(extra)
            }
            remoteViews.setOnClickFillInIntent(R.id.rootLl, clickGroupNameIntent)
            if (currentGroupName == content) {
                remoteViews.setViewVisibility(R.id.checkedIv, View.VISIBLE)
            } else {
                remoteViews.setViewVisibility(R.id.checkedIv, View.GONE)
            }
            builder.addItem(index.toLong(), remoteViews)
            Log.d(
                TAG,
                "getGroupRemoteItems: widgetId:$widgetId content:$content currentGroupName:$currentGroupName"
            )
        }
        return builder.setHasStableIds(true).setViewTypeCount(contentList.count()).build()
    }

}