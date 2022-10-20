package com.terzulli.terzullifilemanager.database.entities

import androidx.room.*

@Entity(
    tableName = "Item",
    foreignKeys = [ForeignKey(
        entity = TableLog::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("fk_log_id"),
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["fk_log_id", "origin_path"], unique = true)]
)
class TableItem(
    @field:ColumnInfo(name = "fk_log_id") var logId: Int,
    @field:ColumnInfo(name = "name") var name: String,
    @field:ColumnInfo(
        name = "origin_path"
    ) var originPath: String,
    @field:ColumnInfo(name = "new_name") var newName: String,
    @field:ColumnInfo(name = "op_failed") var isOpFailed: Boolean
) {
    @PrimaryKey(autoGenerate = true)
    var id = 0

}