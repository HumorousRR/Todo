package com.lrr.todo.listener

import com.lrr.todo.data.models.ToDoData

interface GetTodoDataCallBack {
    fun onSuccess(mapData: Map<String, List<String>>)
}