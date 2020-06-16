package com.mmutert.freshfreezer.data;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class ItemRepository {

    private ItemDao mItemDao;
    private LiveData<List<FrozenItem>> mAllFrozenItems;

    public ItemRepository(Application app) {
        ItemDatabase database = ItemDatabase.getDatabase(app);
        mItemDao = database.itemDao();
        mAllFrozenItems = mItemDao.getAllItems();
    }

    public LiveData<List<FrozenItem>> getAllFrozenItems() {
        return mAllFrozenItems;
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
}
