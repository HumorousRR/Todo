package com.lrr.todo.util

import android.content.Context

object SharedPreferenceUtils {
    private const val SP_WIDGET_DATA = "widget_data"
    private const val SP_ALARM_TIPS = "alarm_tips"

    /**
     * 获取小组件数据
     * @return groupName -- 默认当前groupName
     */
    fun getWidgetData(context: Context): String? {
        val sp = context.getSharedPreferences(SP_WIDGET_DATA, Context.MODE_PRIVATE)
        val key = SP_WIDGET_DATA
        return sp.getString(key, "")
    }

    /**
     * 存储/更新小组件数据
     */
    fun saveWidgetData(context: Context, groupName: String) {
        val sp = context.getSharedPreferences(SP_WIDGET_DATA, Context.MODE_PRIVATE)
        val key = SP_WIDGET_DATA
        sp.edit().putString(key, groupName).apply()
    }

    /**
     * 删除小组件数据
     */
    fun deleteWidgetData(context: Context) {
        val sp = context.getSharedPreferences(SP_WIDGET_DATA, Context.MODE_PRIVATE)
        val key = SP_WIDGET_DATA
        sp.edit().remove(key).apply()
    }

    fun getAlarmTips(context: Context, id: Int): String? {
        val sp = context.getSharedPreferences(SP_ALARM_TIPS, Context.MODE_PRIVATE)
        return sp.getString("$SP_ALARM_TIPS|$id", "")
    }

    /**
     * 保存闹钟数据
     */
    fun saveAlarmData(context: Context, id: Int, date: String) {
        val sp = context.getSharedPreferences(SP_ALARM_TIPS, Context.MODE_PRIVATE)
        sp.edit().putString("$SP_ALARM_TIPS|$id", date).apply()
    }

    /**
     * 删除闹钟数据
     */
    fun removeAlarmData(context: Context, id: Int) {
        val sp = context.getSharedPreferences(SP_ALARM_TIPS, Context.MODE_PRIVATE)
        sp.edit().remove("$SP_ALARM_TIPS|$id").apply()
    }

}