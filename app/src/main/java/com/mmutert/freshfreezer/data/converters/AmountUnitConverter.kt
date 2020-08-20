package com.mmutert.freshfreezer.data.converters

import androidx.room.TypeConverter
import com.mmutert.freshfreezer.data.AmountUnit

object AmountUnitConverter {
    @JvmStatic
    @TypeConverter
    fun fromAmountUnit(unit: AmountUnit): String {
        return unit.toString()
    }

    @JvmStatic
    @TypeConverter
    fun toAmountUnit(unit: String?): AmountUnit {
        return AmountUnit.valueOf(unit!!)
    }
}