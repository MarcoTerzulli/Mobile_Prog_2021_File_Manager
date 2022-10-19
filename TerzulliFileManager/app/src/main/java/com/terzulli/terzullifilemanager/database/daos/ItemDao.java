package com.terzulli.terzullifilemanager.database.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;

import com.terzulli.terzullifilemanager.database.entities.TableItem;

@Dao
public interface ItemDao {

    @Insert
    void insert(TableItem item);

    @Delete
    void delete(TableItem item);
}
