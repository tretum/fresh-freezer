package com.mmutert.freshfreezer.util;


import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;


public class TimeHelper {


    public static LocalDate getCurrentDateLocalized() {
        return LocalDate.now(DateTimeZone.getDefault());
    }
}
