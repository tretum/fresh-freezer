package com.mmutert.freshfreezer.data.converters;

import androidx.room.TypeConverter;

import com.mmutert.freshfreezer.data.TimeOffsetUnit;


public class OffsetUnitConverter {

    @TypeConverter
    public static String fromOffsetUnit(TimeOffsetUnit unit) {
        return unit.toString();
    }

    @TypeConverter
    public static TimeOffsetUnit toOffsetUnit(String unit) {
        return TimeOffsetUnit.valueOf(unit);
    }
}
