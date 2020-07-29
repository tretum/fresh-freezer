package com.mmutert.freshfreezer.util;


import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;


public class TimeHelper {


    public static LocalDate getCurrentDateLocalized() {
        return LocalDate.now(DateTimeZone.getDefault());
    }


    public static LocalDateTime getCurrentDateTimeLocalized() {

        return LocalDateTime.now(DateTimeZone.getDefault());
    }
}
