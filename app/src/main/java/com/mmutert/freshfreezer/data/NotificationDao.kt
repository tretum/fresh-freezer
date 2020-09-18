package com.mmutert.freshfreezer.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NotificationDao {

    @get:Query("Select * from notifications")
    val allNotificationsLiveData: LiveData<List<ItemNotification>>

    @Query("Select * from notifications where item_id = :itemId")
    fun getAllNotificationsLiveData(itemId: Long): LiveData<List<ItemNotification>>

    @Query("Select * from notifications where item_id = :itemId")
    fun getAllNotifications(itemId: Long): List<ItemNotification>

    fun getAllNotificationsLiveData(item: StorageItem): LiveData<List<ItemNotification>> {
        return getAllNotificationsLiveData(item.id)
    }

    fun getAllNotifications(item: StorageItem): List<ItemNotification> {
        return getAllNotifications(item.id)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addNotification(notification: ItemNotification?)

    @Delete
    suspend fun deleteNotification(notification: ItemNotification?)
}