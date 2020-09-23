package com.mmutert.freshfreezer.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface StoredItemDao {
    // ============================== Items ====================================
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: StorageItem): Long

    @get:Query("SELECT * FROM items")
    val allItems: LiveData<List<StorageItem>>

    @Delete
    suspend fun deleteItem(item: StorageItem?)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateFrozenItem(item: StorageItem?)

    @Query("SELECT * FROM items where id = :itemId")
    fun getStoredItem(itemId: Long): StorageItem?

    // ========================= Combined Items and Notifications ============================
    @Transaction
    @Query("Select * from items where id = :id")
    fun getItemAndNotificationsLiveData(id: Long): LiveData<ItemAndNotifications>

    @Transaction
    @Query("Select * from items where id = :id")
    fun getItemAndNotifications(id: Long): ItemAndNotifications?

    @get:Transaction
    @get:Query("Select * from items")
    val itemAndNotificationsList: LiveData<List<ItemAndNotifications>>

}