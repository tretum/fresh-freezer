package com.mmutert.freshfreezer.data.converters;

import androidx.room.TypeConverter;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;


public class LocalDateConverter {

    @TypeConverter
    public static LocalDate toDate(String dateLong) {
        return dateLong == null ? null : LocalDate.parse(dateLong);
    }

    @TypeConverter
    public static String fromDate(LocalDate date) {
        return date == null ? null : date.toString();
    }
}
