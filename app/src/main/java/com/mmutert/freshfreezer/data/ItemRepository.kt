package com.mmutert.freshfreezer.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ItemRepository(private val mStoredItemDao: StoredItemDao,
                     private val mNotificationDao: NotificationDao,
                     private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO) {

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

    suspend fun insertItem(itemToInsert: StorageItem) =
        withContext(ioDispatcher) {
            val rowId = mStoredItemDao.insertItem(itemToInsert)
            itemToInsert.id = rowId
        }

    suspend fun deleteItem(itemToDelete: StorageItem) {
        val allNotifications = getAllNotificationsLiveData(itemToDelete).value!!

        withContext(ioDispatcher) {
            mStoredItemDao.deleteItem(itemToDelete)
        }
        for (allNotification in allNotifications) {
            deleteNotification(allNotification)
        }
    }

    suspend fun updateItem(item: StorageItem) =
        withContext(ioDispatcher) {
            mStoredItemDao.updateFrozenItem(item)
        }

    suspend fun archiveItem(itemToArchive: StorageItem) {
        if (!itemToArchive.isArchived) {
            itemToArchive.isArchived = true
            withContext(ioDispatcher) {
                mStoredItemDao.updateFrozenItem(itemToArchive)
            }
        }
    }

    suspend fun restoreItem(itemToRestore: StorageItem) {
        if (itemToRestore.isArchived) {
            itemToRestore.isArchived = false
            withContext(ioDispatcher) {
                mStoredItemDao.updateFrozenItem(itemToRestore)
            }
        }
    }

    suspend fun getItemAndNotifications(itemId: Long): ItemAndNotifications =
        withContext(ioDispatcher) {
            return@withContext mStoredItemDao.getItemAndNotifications(itemId)!!
        }

    fun getAllNotificationsLiveData(item: StorageItem): LiveData<List<ItemNotification>> {

        // TODO Refactor as Transformation on the notification live data member
        return mNotificationDao.getAllNotificationsLiveData(item)
    }

    fun getAllNotifications(item: StorageItem): List<ItemNotification> {
        return mNotificationDao.getAllNotifications(item)
    }

    /**
     * Add the notification to the database
     *
     * @param notification The notification to add
     */
    suspend fun addNotification(notification: ItemNotification) {
        withContext(ioDispatcher) { mNotificationDao.addNotification(notification) }
    }

    /**
     * Remove the given notification from the database
     *
     * @param notification The notification to remove
     */
    suspend fun deleteNotification(notification: ItemNotification) {
        withContext(ioDispatcher) { mNotificationDao.deleteNotification(notification) }
    }

    fun getItemAndNotificationsLiveData(itemId: Long): LiveData<ItemAndNotifications> {

        // TODO Refactor as Transformation on the notification live data member and create the member
        return mStoredItemDao.getItemAndNotificationsLiveData(itemId)
    }

    suspend fun getStorageItem(itemId: Long): StorageItem =
            // TODO Check to see if there should be some error checking before the assertion
            withContext(ioDispatcher) {
                return@withContext mStoredItemDao.getStoredItem(itemId)!!
            }
}