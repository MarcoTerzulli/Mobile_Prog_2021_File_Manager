package com.terzulli.terzullifilemanager.database

import android.content.Context
import androidx.room.Database
import com.terzulli.terzullifilemanager.database.entities.TableLog
import com.terzulli.terzullifilemanager.database.entities.TableItem
import androidx.room.RoomDatabase
import com.terzulli.terzullifilemanager.database.daos.ItemDao
import com.terzulli.terzullifilemanager.database.daos.LogDao
import com.terzulli.terzullifilemanager.database.LogDatabase
import kotlin.jvm.Synchronized
import androidx.room.Room

@Database(entities = [TableLog::class, TableItem::class], version = 5)
abstract class LogDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao?
    abstract fun logDao(): LogDao?

    companion object {
        const val databaseName = "LogDatabase"
        private var instance: LogDatabase? = null
        @JvmStatic
        @Synchronized
        fun getInstance(context: Context): LogDatabase? {
            if (instance == null) {
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    LogDatabase::class.java, databaseName
                )
                    .fallbackToDestructiveMigration()
                    .build()
            }
            return instance
        }
    }
}