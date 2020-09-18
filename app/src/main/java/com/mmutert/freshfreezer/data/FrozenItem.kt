package com.mmutert.freshfreezer.data

import androidx.room.*
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime

@Entity(tableName = "items")
data class FrozenItem(
        @PrimaryKey(autoGenerate = true)
        var id: Long = 0,

        var name: String = "",

        var amount: Float = 0f,

        var unit: AmountUnit = AmountUnit.GRAMS,

        @ColumnInfo(name = "frozen_date")
        var frozenAtDate: LocalDate? = null,

        @ColumnInfo(name = "best_before_date")
        var bestBeforeDate: LocalDate,

        @ColumnInfo(name = "item_creation_date")
        var itemCreationDate: LocalDateTime,

        @ColumnInfo(name = "last_changed_at_date")
        var lastChangedAtDate: LocalDateTime,

        var notes: String? = null,

        var condition: Condition,

        @ColumnInfo(defaultValue = "false")
        var isArchived: Boolean = false
)