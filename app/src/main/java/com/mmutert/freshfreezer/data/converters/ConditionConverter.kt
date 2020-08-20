package com.mmutert.freshfreezer.data.converters

import androidx.room.TypeConverter
import com.mmutert.freshfreezer.data.Condition

object ConditionConverter {
    @JvmStatic
    @TypeConverter
    fun fromCondition(unit: Condition): String {
        return unit.toString()
    }

    @JvmStatic
    @TypeConverter
    fun toCondition(unit: String?): Condition {
        return Condition.valueOf(unit!!)
    }
}