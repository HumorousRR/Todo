package com.lrr.todo.listener

import com.lrr.todo.data.models.ToDoData

interface GetAlarmDataCallBack {
    fun onSuccess(data: List<ToDoData>)
}