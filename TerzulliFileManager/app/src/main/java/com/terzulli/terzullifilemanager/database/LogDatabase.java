package com.terzulli.terzullifilemanager.database;

import androidx.room.Database;

import com.terzulli.terzullifilemanager.database.daos.ItemDao;
import com.terzulli.terzullifilemanager.database.daos.LogDao;
import com.terzulli.terzullifilemanager.database.entities.TableItem;
import com.terzulli.terzullifilemanager.database.entities.TableLog;

@Database(entities = {TableLog.class, TableItem.class}, version = 1)
public abstract class LogDatabase {
    public abstract ItemDao itemDao();
    public abstract LogDao logDao();
}
