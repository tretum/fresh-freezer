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
}
