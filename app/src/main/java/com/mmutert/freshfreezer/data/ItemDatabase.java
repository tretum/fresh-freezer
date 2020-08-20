package com.mmutert.freshfreezer.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.mmutert.freshfreezer.data.converters.AmountUnitConverter;
import com.mmutert.freshfreezer.data.converters.ConditionConverter;
import com.mmutert.freshfreezer.data.converters.LocalDateConverter;
import com.mmutert.freshfreezer.data.converters.LocalDateTimeConverter;
import com.mmutert.freshfreezer.data.converters.OffsetUnitConverter;
import com.mmutert.freshfreezer.data.converters.UUIDConverter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Database(entities = {FrozenItem.class, ItemNotification.class}, version = 1, exportSchema = false)
@TypeConverters(value = {
        LocalDateTimeConverter.class,
        LocalDateConverter.class,
        AmountUnitConverter.class,
        OffsetUnitConverter.class,
        ConditionConverter.class,
        UUIDConverter.class})
public abstract class ItemDatabase extends RoomDatabase {
    private static ItemDatabase INSTANCE = null;


    public abstract ItemDao itemDao();


    private static final int NUMBER_OF_WRITE_THREADS = 1;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_WRITE_THREADS);


    static ItemDatabase getDatabase(final Context context) {

        if (INSTANCE == null) {
            synchronized (ItemDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            ItemDatabase.class, "item_database"
                    )
                                   .build();
                }
            }
        }
        return INSTANCE;
    }


}
