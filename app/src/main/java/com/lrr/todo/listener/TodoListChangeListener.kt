package com.lrr.todo.listener

import com.lrr.todo.data.models.ToDoData

interface TodoListChangeListener {
    fun onDeleteTodoData(data: ToDoData)

    fun onCheckedDone(data: ToDoData)

    fun onClickAlarm(data: ToDoData, y:Int)
}