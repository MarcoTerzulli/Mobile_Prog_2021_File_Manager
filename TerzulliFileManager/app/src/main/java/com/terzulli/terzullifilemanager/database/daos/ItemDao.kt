package com.terzulli.terzullifilemanager.database.daos

import androidx.room.*
import com.terzulli.terzullifilemanager.database.entities.TableItem

@Dao
interface ItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: TableItem?): Long

    @Update
    fun update(item: TableItem?)

    @Delete
    fun delete(item: TableItem?)
}