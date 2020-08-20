package com.mmutert.freshfreezer.data;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class ItemRepository {

    private final ItemDao mItemDao;
    private final LiveData<List<FrozenItem>> mAllActiveItems;
    private final LiveData<List<FrozenItem>> mAllArchivedFrozenItems;
    private final LiveData<List<ItemNotification>> mAllNotifications;


    public ItemRepository(Application app) {

        ItemDatabase database = ItemDatabase.getDatabase(app);
        mItemDao                = database.itemDao();
        LiveData<List<FrozenItem>> mAllItems = mItemDao.getAllItems();
        mAllArchivedFrozenItems = Transformations.map(mAllItems, input -> {
            ArrayList<FrozenItem> result = new ArrayList<>();
            for (FrozenItem item : input) {
                if(item.isArchived()) {
                    result.add(item);
                }
            }
            return result;
        });
        mAllActiveItems = Transformations.map(mAllItems, input -> {
            ArrayList<FrozenItem> result = new ArrayList<>();
            for (FrozenItem item : input) {
                if(!item.isArchived()) {
                    result.add(item);
                }
            }
            return result;
        });
        mAllNotifications = mItemDao.getAllNotificationsLiveData();
    }


    /**
     * Get all the items that are not marked as archived.
     * @return The active items
     */
    public LiveData<List<FrozenItem>> getAllActiveFrozenItems() {

        return mAllActiveItems;
    }


    public void insertItem(final FrozenItem itemToInsert) {

        ItemDatabase.databaseWriteExecutor.execute(() -> {
            long rowId = mItemDao.insertItem(itemToInsert);
            itemToInsert.setId(rowId);
        });
    }


    public void deleteItem(FrozenItem itemToDelete) {

        List<ItemNotification> allNotifications = getAllNotificationsLiveData(itemToDelete).getValue();

        ItemDatabase.databaseWriteExecutor.execute(() -> {
            mItemDao.deleteItem(itemToDelete);
        });

        for (ItemNotification allNotification : allNotifications) {
            deleteNotification(allNotification);
        }
    }


    public void updateItem(final FrozenItem item) {

        ItemDatabase.databaseWriteExecutor.execute(() -> mItemDao.updateFrozenItem(item));
    }


    public void archiveItem(FrozenItem itemToArchive) {

        if (!itemToArchive.isArchived()) {
            itemToArchive.setArchived(true);

            ItemDatabase.databaseWriteExecutor.execute(() -> mItemDao.updateFrozenItem(itemToArchive));
        }
    }


    public void restoreItem(FrozenItem itemToRestore) {

        if (itemToRestore.isArchived()) {
            itemToRestore.setArchived(false);

            ItemDatabase.databaseWriteExecutor.execute(() -> mItemDao.updateFrozenItem(itemToRestore));
        }
    }


    public ItemAndNotifications getItemAndNotifications(long itemId) {

        return mItemDao.getItemAndNotifications(itemId);
    }


    public LiveData<List<ItemNotification>> getNotifications() {

        return mAllNotifications;
    }


    public LiveData<List<ItemNotification>> getAllNotificationsLiveData(FrozenItem item) {

        // TODO Refactor as Transformation on the notification live data member
        return mItemDao.getAllNotificationsLiveData(item);
    }


    public List<ItemNotification> getAllNotifications(FrozenItem item) {

        return mItemDao.getAllNotifications(item);
    }


    /**
     * Add the notification to the database
     *
     * @param notification The notification to add
     */
    public void addNotification(final ItemNotification notification) {

        ItemDatabase.databaseWriteExecutor.execute(() -> {
            mItemDao.addNotification(notification);
        });
    }


    /**
     * Remove the given notification from the database
     *
     * @param notification The notification to remove
     */
    public void deleteNotification(ItemNotification notification) {

        ItemDatabase.databaseWriteExecutor.execute(() -> {
            mItemDao.deleteNotification(notification);
        });
    }


    public LiveData<List<FrozenItem>> getAllArchivedFrozenItems() {

        return mAllArchivedFrozenItems;
    }


    public LiveData<ItemAndNotifications> getItemAndNotificationsLiveData(final long itemId) {

        // TODO Refactor as Transformation on the notification live data member and create the member
        return mItemDao.getItemAndNotificationsLiveData(itemId);
    }
}
