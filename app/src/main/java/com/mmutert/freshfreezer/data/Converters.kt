package com.mmutert.freshfreezer.data

import androidx.room.TypeConverter
import org.joda.time.LocalDate
import java.util.*

object Converters {
    @TypeConverter
    fun fromCondition(unit: Condition): String {
        return unit.toString()
    }

    @TypeConverter
    fun toCondition(unit: String): Condition {
        return Condition.valueOf(unit)
    }

    @TypeConverter
    fun fromAmountUnit(unit: AmountUnit): String {
        return unit.toString()
    }

    @TypeConverter
    fun toAmountUnit(unit: String): AmountUnit {
        return AmountUnit.valueOf(unit)
    }

    @TypeConverter
    fun toDate(dateLong: String): LocalDate {
        return LocalDate.parse(dateLong)
    }

    @TypeConverter
    fun fromDate(date: LocalDate): String {
        return date.toString()
    }

    @TypeConverter
    fun toUUID(uuid: String): UUID {
        return UUID.fromString(uuid)
    }

    @TypeConverter
    fun fromUUID(uuid: UUID): String {
        return uuid.toString()
    }

    @TypeConverter
    fun fromOffsetUnit(unit: TimeOffsetUnit): String {
        return unit.toString()
    }

    @TypeConverter
    fun toOffsetUnit(unit: String): TimeOffsetUnit {
        return TimeOffsetUnit.valueOf(unit)
    }
}