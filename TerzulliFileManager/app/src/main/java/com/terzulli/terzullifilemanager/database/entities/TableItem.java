package com.terzulli.terzullifilemanager.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "Item",
        foreignKeys = {@ForeignKey(entity = TableLog.class,
        parentColumns = "id",
        childColumns = "fk_log_id",
        onDelete = ForeignKey.CASCADE)},
        indices = {
            @Index(value = {"fk_log_id", "origin_path"}, unique = true)
        }
)
public class TableItem {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "fk_log_id")
    private int logId;
    @ColumnInfo(name = "name")
    @NonNull
    private String name;
    @ColumnInfo(name = "origin_path")
    @NonNull
    private String originPath;
    @ColumnInfo(name = "new_name")
    private String newName;
    @ColumnInfo(name = "op_failed")
    private boolean opFailed;

    public TableItem(int logId, @NonNull String name, @NonNull String originPath, String newName,
                     boolean opFailed) {
        this.logId = logId;
        this.name = name;
        this.originPath = originPath;
        this.newName = newName;
        this.opFailed = opFailed;
    }

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

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public String getOriginPath() {
        return originPath;
    }

    public void setOriginPath(@NonNull String originPath) {
        this.originPath = originPath;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }
}
