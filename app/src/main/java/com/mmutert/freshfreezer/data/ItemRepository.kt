package com.mmutert.freshfreezer.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations

class ItemRepository(private val mItemDao: ItemDao) {

    /**
     * Get all the items that are not marked as archived.
     * @return The active items
     */
    private val mAllItems = mItemDao.allItems
    val allActiveFrozenItems: LiveData<List<FrozenItem>> = Transformations.map(mAllItems) { input: List<FrozenItem> ->
        input.filter { !it.isArchived }
    }

    val allArchivedFrozenItems: LiveData<List<FrozenItem>> = Transformations.map(mAllItems) { input: List<FrozenItem> ->
        input.filter { it.isArchived }
    }
    val notifications: LiveData<List<ItemNotification>> = mItemDao.allNotificationsLiveData

    fun insertItem(itemToInsert: FrozenItem) {
        ItemDatabase.databaseWriteExecutor.execute {
            val rowId = mItemDao.insertItem(itemToInsert)
            itemToInsert.id = rowId
        }
    }

    fun deleteItem(itemToDelete: FrozenItem?) {
        val allNotifications = getAllNotificationsLiveData(itemToDelete).value!!
        ItemDatabase.databaseWriteExecutor.execute { mItemDao.deleteItem(itemToDelete) }
        for (allNotification in allNotifications) {
            deleteNotification(allNotification)
        }
    }

    fun updateItem(item: FrozenItem?) {
        ItemDatabase.databaseWriteExecutor.execute { mItemDao.updateFrozenItem(item) }
    }

    fun archiveItem(itemToArchive: FrozenItem) {
        if (!itemToArchive.isArchived) {
            itemToArchive.isArchived = true
            ItemDatabase.databaseWriteExecutor.execute { mItemDao.updateFrozenItem(itemToArchive) }
        }
    }

    fun restoreItem(itemToRestore: FrozenItem) {
        if (itemToRestore.isArchived) {
            itemToRestore.isArchived = false
            ItemDatabase.databaseWriteExecutor.execute { mItemDao.updateFrozenItem(itemToRestore) }
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
    fun addNotification(notification: ItemNotification?) {
        ItemDatabase.databaseWriteExecutor.execute { mItemDao.addNotification(notification) }
    }

    /**
     * Remove the given notification from the database
     *
     * @param notification The notification to remove
     */
    fun deleteNotification(notification: ItemNotification?) {
        ItemDatabase.databaseWriteExecutor.execute { mItemDao.deleteNotification(notification) }
    }

    fun getItemAndNotificationsLiveData(itemId: Long): LiveData<ItemAndNotifications> {

        // TODO Refactor as Transformation on the notification live data member and create the member
        return mItemDao.getItemAndNotificationsLiveData(itemId)
    }
}