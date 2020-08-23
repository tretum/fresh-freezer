package com.mmutert.freshfreezer.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ItemRepository(private val mItemDao: ItemDao) {

    /**
     * Get all the items that are not marked as archived.
     * @return The active items
     */
    private val mAllItems = mItemDao.allItems
    val allActiveFrozenItems: LiveData<List<FrozenItem>> =
            Transformations.map(mAllItems) { input: List<FrozenItem> ->
                input.filter { !it.isArchived }
            }

    val allArchivedFrozenItems: LiveData<List<FrozenItem>> =
            Transformations.map(mAllItems) { input: List<FrozenItem> ->
                input.filter { it.isArchived }
            }
    val notifications: LiveData<List<ItemNotification>> = mItemDao.allNotificationsLiveData

    suspend fun insertItem(itemToInsert: FrozenItem) {
        withContext(Dispatchers.IO) {
            val rowId = mItemDao.insertItem(itemToInsert)
            itemToInsert.id = rowId
        }
    }

    suspend fun deleteItem(itemToDelete: FrozenItem?) {
        val allNotifications = getAllNotificationsLiveData(itemToDelete).value!!

        withContext(Dispatchers.IO) {
            mItemDao.deleteItem(itemToDelete)
        }
        for (allNotification in allNotifications) {
            deleteNotification(allNotification)
        }
    }

    suspend fun updateItem(item: FrozenItem?) {
        withContext(Dispatchers.IO) {
            mItemDao.updateFrozenItem(item)
        }
    }

    suspend fun archiveItem(itemToArchive: FrozenItem) {
        if (!itemToArchive.isArchived) {
            itemToArchive.isArchived = true
            withContext(Dispatchers.IO) {
                mItemDao.updateFrozenItem(itemToArchive)
            }
        }
    }

    suspend fun restoreItem(itemToRestore: FrozenItem) {
        if (itemToRestore.isArchived) {
            itemToRestore.isArchived = false
            withContext(Dispatchers.IO) {
                mItemDao.updateFrozenItem(itemToRestore)
            }
        }
    }

    fun getItemAndNotifications(itemId: Long): ItemAndNotifications {
        return mItemDao.getItemAndNotifications(itemId)
    }

    fun getAllNotificationsLiveData(item: FrozenItem?): LiveData<List<ItemNotification>> {

        // TODO Refactor as Transformation on the notification live data member
        return mItemDao.getAllNotificationsLiveData(item!!)
    }

    fun getAllNotifications(item: FrozenItem?): List<ItemNotification> {
        return mItemDao.getAllNotifications(item!!)
    }

    /**
     * Add the notification to the database
     *
     * @param notification The notification to add
     */
    suspend fun addNotification(notification: ItemNotification?) {
        withContext(Dispatchers.IO) { mItemDao.addNotification(notification) }
    }

    /**
     * Remove the given notification from the database
     *
     * @param notification The notification to remove
     */
    suspend fun deleteNotification(notification: ItemNotification?) {
        withContext(Dispatchers.IO) { mItemDao.deleteNotification(notification) }
    }

    fun getItemAndNotificationsLiveData(itemId: Long): LiveData<ItemAndNotifications> {

        // TODO Refactor as Transformation on the notification live data member and create the member
        return mItemDao.getItemAndNotificationsLiveData(itemId)
    }
}