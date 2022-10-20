package com.terzulli.terzullifilemanager.database.daos

import androidx.room.*
import com.terzulli.terzullifilemanager.database.converters.DateConverter
import com.terzulli.terzullifilemanager.database.entities.TableLog
import com.terzulli.terzullifilemanager.database.entities.TableItem
import java.util.*

@Dao
@TypeConverters(DateConverter::class)
interface LogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(log: TableLog?): Long

    @Update
    fun update(log: TableLog?)

    @Delete
    fun delete(log: TableLog?)

    @get:Query("SELECT * FROM Log ORDER BY timestamp DESC")
    val all: List<TableLog?>?

    @Query("SELECT Log.id FROM Log WHERE Log.timestamp = :timestamp")
    fun getIdByTimestamp(timestamp: Date?): Int

    @Query("SELECT * FROM Log WHERE id = :id ORDER BY timestamp DESC")
    fun findById(id: Int): TableLog?

    @Query(
        "SELECT * " +
                "FROM Item AS ITEM, Log AS LOG " +
                "WHERE ITEM.fk_log_id = LOG.id " +
                "AND LOG.id = :id " +
                "ORDER BY ITEM.name"
    )
    fun findItemList(id: Int): List<TableItem?>?

    @Query(
        "SELECT * " +
                "FROM Item AS ITEM, Log AS LOG " +
                "WHERE ITEM.fk_log_id = LOG.id " +
                "AND LOG.id = :id " +
                "AND ITEM.op_failed = 1 " +  // True viene mappato a 1
                "ORDER BY ITEM.name"
    )
    fun findFailedItemList(id: Int): List<TableItem?>?

    @Query(
        "SELECT COUNT(*) " +
                "FROM Item AS ITEM, Log AS LOG " +
                "WHERE ITEM.fk_log_id = LOG.id " +
                "AND LOG.id = :id "
    )
    fun countItems(id: Int): Int

    @Query(
        "SELECT COUNT(*) " +
                "FROM Item AS ITEM, Log AS LOG " +
                "WHERE ITEM.fk_log_id = LOG.id " +
                "AND LOG.id = :id " +
                "AND ITEM.op_failed = 1"
    )
    fun countFailedItems(id: Int): Int

    @Query("DELETE FROM Log WHERE timestamp < :specifiedDate")
    fun deleteLogsOlderThanDate(specifiedDate: Date?)

    @Query("DELETE FROM Log")
    fun deleteAllLogs()
}