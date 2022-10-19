package com.terzulli.terzullifilemanager.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "Item", foreignKeys = {@ForeignKey(entity = TableLog.class,
        parentColumns = "id",
        childColumns = "fk_log_id",
        onDelete = ForeignKey.CASCADE)})
public class TableItem {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "fk_log_id")
    private String logId;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "origin_path")
    private String originPath;

    @ColumnInfo(name = "new_name")
    private String newName;

    @ColumnInfo(name = "op_failed")
    private boolean opFailed;

    public boolean isOpFailed() {
        return opFailed;
    }

    public void setOpFailed(boolean opFailed) {
        this.opFailed = opFailed;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOriginPath() {
        return originPath;
    }

    public void setOriginPath(String originPath) {
        this.originPath = originPath;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }
}
