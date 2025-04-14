package com.lrr.todo.data.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "todo_table")
@Parcelize
data class ToDoData(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    /**
     * 分组名称
     */
    var groupName: String,
    /**
     * 待办内容
     */
    var content: String = "",
    /**
     * 提醒时间(格式：yyyy-MM-dd HH:mm)
     */
    var remindTime: String = "",
    /**
     * 是否已完成
     */
    var hasDone: Boolean = false,

) : Parcelable {

    override fun toString(): String {
        return "groupName:$groupName,content:$content,hasDone:$hasDone,remindTime:$remindTime"
    }
}