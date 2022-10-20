package com.terzulli.terzullifilemanager.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.terzulli.terzullifilemanager.database.daos.ItemDao;
import com.terzulli.terzullifilemanager.database.daos.LogDao;
import com.terzulli.terzullifilemanager.database.entities.TableItem;
import com.terzulli.terzullifilemanager.database.entities.TableLog;

@Database(entities = {TableLog.class, TableItem.class}, version = 2)
public abstract class LogDatabase extends RoomDatabase {
    public abstract ItemDao itemDao();
    public abstract LogDao logDao();
    public static final String databaseName = "LogDatabase";

    private static LogDatabase instance;

    public static synchronized LogDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                                    LogDatabase.class, databaseName)
                            .fallbackToDestructiveMigration()
                            .build();
        }
        return instance;
    }

}
