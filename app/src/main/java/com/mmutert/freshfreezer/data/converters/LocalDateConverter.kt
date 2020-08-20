package com.mmutert.freshfreezer.data.converters

import androidx.room.TypeConverter
import org.joda.time.LocalDate

object LocalDateConverter {
    @JvmStatic
    @TypeConverter
    fun toDate(dateLong: String?): LocalDate? {
        return if (dateLong == null) null else LocalDate.parse(dateLong)
    }

    @JvmStatic
    @TypeConverter
    fun fromDate(date: LocalDate?): String? {
        return date?.toString()
    }
}