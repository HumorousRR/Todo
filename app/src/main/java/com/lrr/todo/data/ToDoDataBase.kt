package com.lrr.todo.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lrr.todo.data.models.ToDoData

@Database(entities = [ToDoData::class], version = 1, exportSchema = false)
abstract class ToDoDataBase : RoomDatabase() {
    abstract fun todoDAO(): ToDoDao

    companion object {
        @Volatile
        private var INSTANCE: ToDoDataBase? = null
        fun getDataBase(context: Context): ToDoDataBase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ToDoDataBase::class.java,
                    "todo_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}