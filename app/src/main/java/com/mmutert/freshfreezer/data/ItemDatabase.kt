package com.mmutert.freshfreezer.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mmutert.freshfreezer.data.converters.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Database(entities = [FrozenItem::class, ItemNotification::class], version = 1, exportSchema = false)
@TypeConverters(value = [LocalDateTimeConverter::class, LocalDateConverter::class, AmountUnitConverter::class, OffsetUnitConverter::class, ConditionConverter::class, UUIDConverter::class])
abstract class ItemDatabase : RoomDatabase() {

    abstract fun itemDao(): ItemDao

    companion object {
        @Volatile
        private var INSTANCE: ItemDatabase? = null

        private const val NUMBER_OF_WRITE_THREADS = 1

        @JvmField
        val databaseWriteExecutor: ExecutorService = Executors.newFixedThreadPool(NUMBER_OF_WRITE_THREADS)

        @JvmStatic
        fun getDatabase(context: Context): ItemDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        ItemDatabase::class.java,
                        "item_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}