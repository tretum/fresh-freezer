package com.mmutert.freshfreezer.data.converters;

import androidx.room.TypeConverter;

import java.util.Date;
import java.util.UUID;


public class UUIDConverter {

    @TypeConverter
    public static UUID toUUID(String uuid){
        return uuid == null ? null: UUID.fromString(uuid);
    }

    @TypeConverter
    public static String fromUUID(UUID uuid){
        return uuid == null ? null : uuid.toString();
    }
}
