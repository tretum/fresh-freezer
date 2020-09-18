package com.mmutert.freshfreezer.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ItemRepository(private val mStoredItemDao: StoredItemDao, private val mNotificationDao: NotificationDao) {

    /**
     * Get all the items that are not marked as archived.
     * @return The active items
     */
    private val mAllItems = mStoredItemDao.allItems
    val allActiveStorageItems: LiveData<List<StorageItem>> =
            Transformations.map(mAllItems) { input: List<StorageItem> ->
                input.filter { !it.isArchived }
            }

    val allArchivedStorageItems: LiveData<List<StorageItem>> =
            Transformations.map(mAllItems) { input: List<StorageItem> ->
                input.filter { it.isArchived }
            }
    val notifications: LiveData<List<ItemNotification>> = mNotificationDao.allNotificationsLiveData

    suspend fun insertItem(itemToInsert: StorageItem) {
        withContext(Dispatchers.IO) {
            val rowId = mStoredItemDao.insertItem(itemToInsert)
            itemToInsert.id = rowId
        }
    }

    suspend fun deleteItem(itemToDelete: StorageItem?) {
        val allNotifications = getAllNotificationsLiveData(itemToDelete).value!!

        withContext(Dispatchers.IO) {
            mStoredItemDao.deleteItem(itemToDelete)
        }
        for (allNotification in allNotifications) {
            deleteNotification(allNotification)
        }
    }

    suspend fun updateItem(item: StorageItem?) {
        withContext(Dispatchers.IO) {
            mStoredItemDao.updateFrozenItem(item)
        }
    }

    suspend fun archiveItem(itemToArchive: StorageItem) {
        if (!itemToArchive.isArchived) {
            itemToArchive.isArchived = true
            withContext(Dispatchers.IO) {
                mStoredItemDao.updateFrozenItem(itemToArchive)
            }
        }
    }

    suspend fun restoreItem(itemToRestore: StorageItem) {
        if (itemToRestore.isArchived) {
            itemToRestore.isArchived = false
            withContext(Dispatchers.IO) {
                mStoredItemDao.updateFrozenItem(itemToRestore)
            }
        }
    }

    fun getItemAndNotifications(itemId: Long): ItemAndNotifications {
        return mStoredItemDao.getItemAndNotifications(itemId)
    }

    fun getAllNotificationsLiveData(item: StorageItem?): LiveData<List<ItemNotification>> {

        // TODO Refactor as Transformation on the notification live data member
        return mNotificationDao.getAllNotificationsLiveData(item!!)
    }

    fun getAllNotifications(item: StorageItem?): List<ItemNotification> {
        return mNotificationDao.getAllNotifications(item!!)
    }

    /**
     * Add the notification to the database
     *
     * @param notification The notification to add
     */
    suspend fun addNotification(notification: ItemNotification?) {
        withContext(Dispatchers.IO) { mNotificationDao.addNotification(notification) }
    }

    /**
     * Remove the given notification from the database
     *
     * @param notification The notification to remove
     */
    suspend fun deleteNotification(notification: ItemNotification?) {
        withContext(Dispatchers.IO) { mNotificationDao.deleteNotification(notification) }
    }

    fun getItemAndNotificationsLiveData(itemId: Long): LiveData<ItemAndNotifications> {

        // TODO Refactor as Transformation on the notification live data member and create the member
        return mStoredItemDao.getItemAndNotificationsLiveData(itemId)
    }
}