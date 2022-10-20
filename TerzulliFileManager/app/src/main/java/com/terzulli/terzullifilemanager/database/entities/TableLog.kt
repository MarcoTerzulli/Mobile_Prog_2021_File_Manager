package com.terzulli.terzullifilemanager.database.entities

import androidx.room.*
import com.terzulli.terzullifilemanager.database.converters.DateConverter
import java.util.*

@Entity(tableName = "Log", indices = [Index(value = arrayOf("timestamp"), unique = true)])
@TypeConverters(
    DateConverter::class
)
class TableLog(
    @field:ColumnInfo(name = "timestamp") var timestamp: Date,
    @field:ColumnInfo(name = "operation_success") var operationSuccess: Boolean,
    @field:ColumnInfo(
        name = "type"
    ) var operationType: String,
    @field:ColumnInfo(name = "origin_path") var originPath: String,
    @field:ColumnInfo(name = "destination_path") var destinationPath: String,
    @field:ColumnInfo(
        name = "description"
    ) var description: String
) {
    @PrimaryKey(autoGenerate = true)
    var id = 0

    @ColumnInfo(name = "retried")
    var isRetried = false

}