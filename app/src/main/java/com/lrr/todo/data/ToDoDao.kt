package com.lrr.todo.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.lrr.todo.data.models.GroupListBean
import com.lrr.todo.data.models.ToDoData

@Dao
interface ToDoDao {
    @Query("SELECT * FROM todo_table ORDER BY id ASC")
    fun getAllData(): LiveData<MutableList<ToDoData>>

    @Query("SELECT * FROM todo_table ORDER BY id ASC")
    fun getAllTodoData(): MutableList<ToDoData>

    @Query("SELECT * FROM todo_table WHERE hasDone = 0 AND content != '' ORDER BY id ASC")
    fun getAllUndoneData(): MutableList<ToDoData>

    @Query("SELECT groupName, COUNT(CASE WHEN content != '' THEN 1 ELSE NULL END) as listCount FROM todo_table GROUP BY groupName ORDER BY id ASC")
    fun getAllGroup(): LiveData<MutableList<GroupListBean>>

    @Query("SELECT * FROM todo_table WHERE remindTime != '' AND hasDone = 0 AND content != '' ORDER BY id ASC")
    fun getAllAlarm(): MutableList<ToDoData>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertData(toDoData: ToDoData)

    @Update
    suspend fun updateData(toDoData: ToDoData)

    @Delete
    suspend fun deleteItem(toDoData: ToDoData)

    @Query("DELETE FROM todo_table WHERE groupName = :groupName")
    suspend fun deleteGroup(groupName: String)

    @Query("DELETE FROM todo_table")
    suspend fun deleteAll()

    @Query("UPDATE todo_table SET groupName = :newName WHERE groupName = :oldName")
    suspend fun updateGroupName(oldName: String, newName: String)
}