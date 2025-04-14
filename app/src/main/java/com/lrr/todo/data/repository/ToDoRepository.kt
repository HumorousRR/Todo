package com.lrr.todo.data.repository

import android.app.Application
import com.lrr.todo.data.ToDoDataBase
import com.lrr.todo.data.models.ToDoData

class ToDoRepository(application: Application) {
    private val toDoDao = ToDoDataBase.getDataBase(application).todoDAO()
    val getAllData = toDoDao.getAllData()

    suspend fun insertData(toDoData: ToDoData) {
        toDoDao.insertData(toDoData)
    }

    suspend fun updateData(toDoData: ToDoData) {
        toDoDao.updateData(toDoData)
    }

    suspend fun deleteItem(toDoData: ToDoData) {
        toDoDao.deleteItem(toDoData)
    }

    suspend fun deleteGroup(groupName: String) {
        toDoDao.deleteGroup(groupName)
    }

    suspend fun deleteAll() {
        toDoDao.deleteAll()
    }

    suspend fun updateGroupName(oldName: String, newName: String) {
        toDoDao.updateGroupName(oldName, newName)
    }

    fun getAllUndoneData(): MutableList<ToDoData> {
        return toDoDao.getAllUndoneData()
    }

    fun getAllTodoData(): MutableList<ToDoData> {
        return toDoDao.getAllTodoData()
    }

    fun getAllAlarm(): MutableList<ToDoData> {
        return toDoDao.getAllAlarm()
    }
}