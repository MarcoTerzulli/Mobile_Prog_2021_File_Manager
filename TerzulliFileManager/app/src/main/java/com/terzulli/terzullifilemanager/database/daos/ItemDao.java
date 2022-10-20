package com.terzulli.terzullifilemanager.database.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Update;

import com.terzulli.terzullifilemanager.database.entities.TableItem;

@Dao
public interface ItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(TableItem item);

    @Update
    void update(TableItem item);

    @Delete
    void delete(TableItem item);
}
