package com.mmutert.freshfreezer.data.converters;

import androidx.room.TypeConverter;

import com.mmutert.freshfreezer.data.Condition;


public class ConditionConverter {

    @TypeConverter
    public static String fromCondition(Condition unit) {

        return unit.toString();
    }


    @TypeConverter
    public static Condition toCondition(String unit) {

        return Condition.valueOf(unit);
    }
}
