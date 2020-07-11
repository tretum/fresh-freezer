package com.mmutert.freshfreezer.data.converters;

import androidx.room.TypeConverter;

import com.mmutert.freshfreezer.data.AmountUnit;

public class AmountUnitConverter {

    @TypeConverter
    public static String fromAmountUnit(AmountUnit unit) {
        return unit.toString();
    }

    @TypeConverter
    public static AmountUnit toAmountUnit(String unit) {
        return AmountUnit.valueOf(unit);
    }
}
