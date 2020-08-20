package com.mmutert.freshfreezer.data.converters

import androidx.room.TypeConverter
import java.util.*

object UUIDConverter {
    @JvmStatic
    @TypeConverter
    fun toUUID(uuid: String?): UUID? {
        return if (uuid == null) null else UUID.fromString(uuid)
    }

    @JvmStatic
    @TypeConverter
    fun fromUUID(uuid: UUID?): String? {
        return uuid?.toString()
    }
}