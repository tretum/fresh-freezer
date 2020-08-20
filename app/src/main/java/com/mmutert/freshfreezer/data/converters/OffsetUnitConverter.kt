package com.mmutert.freshfreezer.data.converters

import androidx.room.TypeConverter
import com.mmutert.freshfreezer.data.TimeOffsetUnit

object OffsetUnitConverter {
    @JvmStatic
    @TypeConverter
    fun fromOffsetUnit(unit: TimeOffsetUnit): String {
        return unit.toString()
    }

    @JvmStatic
    @TypeConverter
    fun toOffsetUnit(unit: String?): TimeOffsetUnit {
        return TimeOffsetUnit.valueOf(unit!!)
    }
}