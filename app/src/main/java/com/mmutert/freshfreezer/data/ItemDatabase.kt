package com.mmutert.freshfreezer.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [StorageItem::class, ItemNotification::class], version = 1, exportSchema = false)
@TypeConverters(value = [Converters::class])
abstract class ItemDatabase : RoomDatabase() {

    abstract fun itemDao(): StoredItemDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: ItemDatabase? = null

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