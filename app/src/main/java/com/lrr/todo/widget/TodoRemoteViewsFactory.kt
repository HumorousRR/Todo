package com.lrr.todo.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.lrr.todo.R
import com.lrr.todo.util.SharedPreferenceUtils
import com.lrr.todo.widget.TodoWidgetProvider.Companion.EXTRA_GROUP_LIST
import com.lrr.todo.widget.TodoWidgetProvider.Companion.EXTRA_GROUP_NAME
import com.lrr.todo.widget.TodoWidgetProvider.Companion.EXTRA_WIDGET_ID

class TodoRemoteViewsFactory(
    private val context: Context,
    intent: Intent
) : RemoteViewsService.RemoteViewsFactory {
    private val appWidgetId: Int =
        intent.getIntExtra(
            EXTRA_WIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
    private var groupListStr = intent.getStringExtra(EXTRA_GROUP_LIST)
    private val groupList = ArrayList<String>()
    private lateinit var currentGroupName: String

    override fun onCreate() {
        groupListStr?.let {
            groupListStr = it.substring(1, it.length - 1)
            val list = groupListStr!!.split(',')
            list.forEach { groupName ->
                groupList.add(groupName.trim())
            }
        }
        currentGroupName = SharedPreferenceUtils.getWidgetData(context).toString()
    }

    override fun onDataSetChanged() {
    }

    override fun onDestroy() {
        groupList.clear()
    }

    override fun getCount(): Int {
        return groupList.size
    }

    override fun getViewAt(positon: Int): RemoteViews {
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_group_item)
        remoteViews.setTextViewText(R.id.groupNameTv, groupList[positon])
        val fillInIntent = Intent().apply {
            Bundle().also { extras ->
                extras.putInt(EXTRA_WIDGET_ID, appWidgetId)
                extras.putString(EXTRA_GROUP_NAME, groupList[positon])
                putExtras(extras)
            }
        }
        remoteViews.setOnClickFillInIntent(R.id.rootLl, fillInIntent)
        if (currentGroupName == groupList[positon]) {
            remoteViews.setViewVisibility(R.id.checkedIv, View.VISIBLE)
        } else {
            remoteViews.setViewVisibility(R.id.checkedIv, View.GONE)
        }
        return remoteViews
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(positon: Int): Long {
        return positon.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

}