package com.mmutert.freshfreezer.data;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class ItemRepository {

    private ItemDao mItemDao;
    private LiveData<List<FrozenItem>> mAllActiveFrozenItems;
    private LiveData<List<FrozenItem>> mAllArchivedFrozenItems;

    public ItemRepository(Application app) {
        ItemDatabase database = ItemDatabase.getDatabase(app);
        mItemDao              = database.itemDao();
        mAllActiveFrozenItems = mItemDao.getAllActiveItems();
        mAllArchivedFrozenItems = mItemDao.getArchivedItems();
    }

    public LiveData<List<FrozenItem>> getAllActiveFrozenItems() {
        return mAllActiveFrozenItems;
    }

    public void insertItem(final FrozenItem itemToInsert) {
        ItemDatabase.databaseWriteExecutor.execute(() -> {
            long rowId = mItemDao.insertItem(itemToInsert);
            itemToInsert.setId(rowId);
        });
    }

    public void deleteItem(FrozenItem itemToDelete) {
        ItemDatabase.databaseWriteExecutor.execute(() -> {
            mItemDao.deleteItem(itemToDelete);
        });
    }

    public void archiveItem(FrozenItem itemToArchive) {
        if(!itemToArchive.isArchived()) {
            itemToArchive.setArchived(true);

            ItemDatabase.databaseWriteExecutor.execute(() -> {
                mItemDao.updateFrozenItem(itemToArchive);
            });
        }
    }

    public void restoreItem(FrozenItem itemToRestore){
        if(itemToRestore.isArchived()){
            itemToRestore.setArchived(false);

            ItemDatabase.databaseWriteExecutor.execute(() -> {
                mItemDao.updateFrozenItem(itemToRestore);
            });
        }
    }

    public LiveData<List<ItemNotification>> getNotifications(){
        return mItemDao.getAllNotifications();
    }

    public LiveData<List<ItemNotification>> getAllNotifications(FrozenItem item) {
        return mItemDao.getAllNotifications(item);
    }

    public void addNotification(final ItemNotification notification) {
        ItemDatabase.databaseWriteExecutor.execute(() -> {
            mItemDao.addNotification(notification);
        });
    }
    public void deleteNotification(ItemNotification notification) {
        ItemDatabase.databaseWriteExecutor.execute(() -> {
            mItemDao.deleteNotification(notification);
        });
    }

    public LiveData<List<FrozenItem>> getmAllArchivedFrozenItems() {
        return mAllArchivedFrozenItems;
    }
}
