package com.mmutert.freshfreezer.data.converters;

import androidx.room.TypeConverter;

import com.mmutert.freshfreezer.data.AmountUnit;

public class AmountUnitConverter {

    @TypeConverter
    public static Integer fromAmountUnit(AmountUnit unit) {
        switch (unit) {
            case GRAMS:
                return 1;
            case KILOGRAMS:
                return 2;
            case PIECES:
                return 3;
            default: return 0;
        }
    }

    @TypeConverter
    public static AmountUnit toAmountUnit(Integer unit) {
        switch (unit) {
            case 1: return AmountUnit.GRAMS;
            case 2: return AmountUnit.KILOGRAMS;
            case 3: return AmountUnit.PIECES;
            default: return AmountUnit.GRAMS;
        }
    }
}
