package com.mmutert.freshfreezer.data

import androidx.room.*
import java.util.*

@Entity(tableName = "notifications",
        foreignKeys = [
                ForeignKey(
                        entity = StorageItem::class,
                        parentColumns = ["id"],
                        childColumns = ["item_id"],
                        onUpdate = ForeignKey.CASCADE)
        ],
        indices = [
                Index(
                        name = "ItemIdIndex",
                        value = ["item_id"])
        ]
)
data class ItemNotification(
        @PrimaryKey(autoGenerate = true)
        var id: Long,

        @field:ColumnInfo(name = "notification_id")
        var notificationId: UUID?,

        @field:ColumnInfo(name = "item_id")
        var itemId: Long,

        @field:ColumnInfo(name = "time_offset_unit")
        val timeOffsetUnit: TimeOffsetUnit,

        @field:ColumnInfo(name = "offset_amount")
        val offsetAmount: Int
)