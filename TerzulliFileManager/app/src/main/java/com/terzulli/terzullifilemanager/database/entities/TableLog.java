package com.terzulli.terzullifilemanager.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.terzulli.terzullifilemanager.database.converters.DateConverter;

import java.util.Date;

@Entity(tableName = "Log")
@TypeConverters({DateConverter.class})
public class TableLog {
    public TableLog(Date timestamp, String result, String operationType, String originPath, String destinationPath, String description) {
        this.timestamp = timestamp;
        this.result = result;
        this.operationType = operationType;
        this.originPath = originPath;
        this.destinationPath = destinationPath;
        this.description = description;
    }

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "timestamp")
    private Date timestamp;

    @ColumnInfo(name = "result")
    private String result;

    @ColumnInfo(name = "type")
    private String operationType;

    @ColumnInfo(name = "origin_path")
    private String originPath;

    @ColumnInfo(name = "destination_path")
    private String destinationPath;

    @ColumnInfo(name = "retried")
    private boolean retried;

    @ColumnInfo(name = "description")
    private String description;

    public boolean isRetried() {
        return retried;
    }

    public void setRetried(boolean retried) {
        this.retried = retried;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getOriginPath() {
        return originPath;
    }

    public void setOriginPath(String originPath) {
        this.originPath = originPath;
    }

    public String getDestinationPath() {
        return destinationPath;
    }

    public void setDestinationPath(String destinationPath) {
        this.destinationPath = destinationPath;
    }
}
