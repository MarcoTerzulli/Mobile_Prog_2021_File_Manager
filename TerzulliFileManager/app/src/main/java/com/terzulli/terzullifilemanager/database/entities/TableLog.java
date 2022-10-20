package com.terzulli.terzullifilemanager.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.terzulli.terzullifilemanager.database.converters.DateConverter;

import java.util.Date;

@Entity(tableName = "Log",
        indices = {
                @Index(value = "timestamp", unique = true)
        })
@TypeConverters({DateConverter.class})
public class TableLog {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "timestamp")
    @NonNull
    private Date timestamp;
    @ColumnInfo(name = "operation_success")
    private boolean operationSuccess;
    @ColumnInfo(name = "type")
    @NonNull
    private String operationType;
    @ColumnInfo(name = "origin_path")
    private String originPath;
    @ColumnInfo(name = "destination_path")
    private String destinationPath;
    @ColumnInfo(name = "retried")
    private boolean retried;
    @ColumnInfo(name = "description")
    @NonNull
    private String description;

    public TableLog(@NonNull Date timestamp, boolean operationSuccess, @NonNull String operationType,
                    String originPath, String destinationPath, @NonNull String description) {
        this.timestamp = timestamp;
        this.operationSuccess = operationSuccess;
        this.operationType = operationType;
        this.originPath = originPath;
        this.destinationPath = destinationPath;
        this.description = description;
    }

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

    @NonNull
    public String getDescription() {
        return description;
    }

    public void setDescription(@NonNull String description) {
        this.description = description;
    }

    @NonNull
    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(@NonNull Date timestamp) {
        this.timestamp = timestamp;
    }

    public boolean getOperationSuccess() {
        return operationSuccess;
    }

    public void setOperationSuccess(boolean operationSuccess) {
        this.operationSuccess = operationSuccess;
    }

    @NonNull
    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(@NonNull String operationType) {
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
