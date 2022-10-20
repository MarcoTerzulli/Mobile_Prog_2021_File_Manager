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

    // on below line we are getting instance for our database.
    public static synchronized LogDatabase getInstance(Context context) {
        // below line is to check if
        // the instance is null or not.
        if (instance == null) {
            // if the instance is null we
            // are creating a new instance
            instance =
                    // for creating a instance for our database
                    // we are creating a database builder and passing
                    // our database class with our database name.
                    Room.databaseBuilder(context.getApplicationContext(),
                                    LogDatabase.class, databaseName)
                            .fallbackToDestructiveMigration()
                            //.addCallback(roomCallback)
                            .build();
        }
        // after creating an instance
        // we are returning our instance
        return instance;
    }

    /*private static LogDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
        }
    };*/

}
