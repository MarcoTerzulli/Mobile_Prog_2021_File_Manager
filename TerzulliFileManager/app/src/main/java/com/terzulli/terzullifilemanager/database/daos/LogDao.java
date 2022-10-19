package com.terzulli.terzullifilemanager.database.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.terzulli.terzullifilemanager.database.entities.TableItem;
import com.terzulli.terzullifilemanager.database.entities.TableLog;

import java.util.Date;
import java.util.List;

@Dao
public interface LogDao {
    @Query("SELECT * FROM Log ORDER BY timestamp DESC")
    List<TableLog> getAll();

    @Query("SELECT * FROM Log WHERE id = :id ORDER BY timestamp DESC")
    TableLog findById(int id);

    @Query("SELECT * " +
            "FROM Item AS ITEM, Log AS LOG " +
            "WHERE ITEM.fk_log_id = LOG.id " +
            "AND LOG.id = :id " +
            "ORDER BY ITEM.name")
    List<TableItem> findItemList(int id);

    @Query("SELECT * " +
            "FROM Item AS ITEM, Log AS LOG " +
            "WHERE ITEM.fk_log_id = LOG.id " +
            "AND LOG.id = :id " +
            "AND ITEM.op_failed = 1 " + // True viene mappato a 1
            "ORDER BY ITEM.name")
    List<TableItem> findFailedItemList(int id);

    @Query("SELECT COUNT(*) " +
            "FROM Item AS ITEM, Log AS LOG " +
            "WHERE ITEM.fk_log_id = LOG.id " +
            "AND LOG.id = :id ")
    int countItems(int id);

    @Query("SELECT COUNT(*) " +
            "FROM Item AS ITEM, Log AS LOG " +
            "WHERE ITEM.fk_log_id = LOG.id " +
            "AND LOG.id = :id " +
            "AND ITEM.op_failed = 1")
    int countFailedItems(int id);

    @Query("DELETE FROM Log WHERE timestamp < :specifiedDate")
    void deleteLogsOlderThanDate(Date specifiedDate);

    @Insert
    void insert(TableLog log);

    @Delete
    void delete(TableLog log);
}
