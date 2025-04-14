package com.lrr.todo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.lrr.todo.GlobalApplication
import com.lrr.todo.R
import com.lrr.todo.data.models.ToDoData
import com.lrr.todo.data.repository.ToDoRepository
import com.lrr.todo.listener.GetAlarmDataCallBack
import com.lrr.todo.listener.GetTodoDataCallBack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TodoViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ToDoRepository = ToDoRepository(application)

    val getAllData: LiveData<MutableList<ToDoData>> = repository.getAllData
    val groupNameDuplicateTips by lazy { GlobalApplication.getContextInstance().resources.getString(
        R.string.group_name_duplicate_tips) }

    fun insertData(toDoData: ToDoData) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertData(toDoData)
        }
    }

    fun updateData(toDoData: ToDoData) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateData(toDoData)
        }
    }

    fun deleteItem(toDoData: ToDoData) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteItem(toDoData)
        }
    }

    fun deleteGroup(groupName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteGroup(groupName)
        }
    }

    fun deleteAll() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAll()
        }
    }

    fun updateGroupName(oldName: String, newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateGroupName(oldName, newName)
        }
    }

    fun checkCanRenameGroupName(newName: String):Boolean {
        return getAllData.value?.none { it.groupName == newName } ==true
    }

    fun getWidgetDataMap(callback: GetTodoDataCallBack) {
        viewModelScope.launch(Dispatchers.IO) {
            val todoMap = HashMap<String, List<ToDoData>>()
            val allList = repository.getAllTodoData()
            allList.forEach {
                if (todoMap.containsKey(it.groupName)) {
                    val list = todoMap[it.groupName] as MutableList
                    if (it.content.isNotEmpty()) {
                        list.add(it)
                    }
                } else {
                    val list = ArrayList<ToDoData>()
                    if (it.content.isNotEmpty()) {
                        list.add(it)
                    }
                    todoMap[it.groupName] = list
                }
            }
            callback.onSuccess(getWidgetDataMap(todoMap))
        }
    }

    fun getWidgetDataMap(mapData: Map<String, List<ToDoData>>): Map<String, List<String>> {
        val dataMap = HashMap<String, List<String>>()
        mapData.mapValues {
            val undoneList = ArrayList<String>()
            it.value.forEach { data ->
                if (!data.hasDone) {
                    undoneList.add(data.content)
                }
            }
            dataMap.put(it.key, undoneList)
        }
        return dataMap
    }

    fun getAlarmData(callBack: GetAlarmDataCallBack) {
        viewModelScope.launch(Dispatchers.IO) {
            callBack.onSuccess(repository.getAllAlarm())
        }
    }
}