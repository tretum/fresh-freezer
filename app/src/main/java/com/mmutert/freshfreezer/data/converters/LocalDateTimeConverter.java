package com.mmutert.freshfreezer.data.converters;

import androidx.room.TypeConverter;

import org.joda.time.LocalDateTime;


public class LocalDateTimeConverter {

    @TypeConverter
    public static LocalDateTime toDate(String dateLong) {
        return dateLong == null ? null : LocalDateTime.parse(dateLong);
    }

    @TypeConverter
    public static String fromDate(LocalDateTime date) {
        return date == null ? null : date.toString();
    }
}
