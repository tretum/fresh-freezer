package com.mmutert.freshfreezer.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ItemDao {
    // ============================== Items ====================================
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: FrozenItem): Long

    @get:Query("SELECT * FROM items")
    val allItems: LiveData<List<FrozenItem>>

    @Delete
    suspend fun deleteItem(item: FrozenItem?)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateFrozenItem(item: FrozenItem?)

    // ============================ Notifications =================================
    @get:Query("Select * from notifications")
    val allNotificationsLiveData: LiveData<List<ItemNotification>>

    @Query("Select * from notifications where item_id = :itemId")
    fun getAllNotificationsLiveData(itemId: Long): LiveData<List<ItemNotification>>

    @Query("Select * from notifications where item_id = :itemId")
    fun getAllNotifications(itemId: Long): List<ItemNotification>

    fun getAllNotificationsLiveData(item: FrozenItem): LiveData<List<ItemNotification>> {
        return getAllNotificationsLiveData(item.id)
    }

    fun getAllNotifications(item: FrozenItem): List<ItemNotification> {
        return getAllNotifications(item.id)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addNotification(notification: ItemNotification?)

    @Delete
    suspend fun deleteNotification(notification: ItemNotification?)

    // ========================= Combined Items and Notifications ============================
    @Transaction
    @Query("Select * from items where id = :id")
    fun getItemAndNotificationsLiveData(id: Long): LiveData<ItemAndNotifications>

    @Transaction
    @Query("Select * from items where id = :id")
    fun getItemAndNotifications(id: Long): ItemAndNotifications

    @get:Transaction
    @get:Query("Select * from items")
    val itemAndNotificationsList: LiveData<List<ItemAndNotifications>>

}