package com.mmutert.freshfreezer.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.mmutert.freshfreezer.data.converters.ItemAndNotifications;

import java.util.List;

@Dao
public abstract class ItemDao {

    public static final int TRUE = 1;
    public static final int FALSE = 0;

    // ============================== Items ====================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract long insertItem(FrozenItem item);

    @Query("SELECT * FROM items where archived IS 0")
    public abstract LiveData<List<FrozenItem>> getAllActiveItems();

    @Query("SELECT * FROM items WHERE name = :name AND archived is 0")
    public abstract LiveData<List<FrozenItem>> getAllActiveItemsWithName(String name);

    @Query("SELECT * FROM items where archived is 0 ORDER BY best_before_date ASC LIMIT :numResults")
    public abstract LiveData<List<FrozenItem>> getClosestBestBefore(int numResults);

    @Query("SELECT * FROM items Where archived IS 1")
    public abstract LiveData<List<FrozenItem>> getArchivedItems();

    @Delete
    public abstract void deleteItem(FrozenItem item);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    public abstract void updateFrozenItem(FrozenItem item);


    // ============================ Notifications =================================

    @Query("Select * from notifications")
    public abstract LiveData<List<ItemNotification>> getAllNotifications();

    @Query("Select * from notifications where item_id = :itemId")
    public abstract LiveData<List<ItemNotification>> getAllNotifications(long itemId);

    public LiveData<List<ItemNotification>> getAllNotifications(FrozenItem item) {
        return getAllNotifications(item.getId());
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void addNotification(ItemNotification notification);

    @Delete
    public abstract void deleteNotification(ItemNotification notification);


    // ========================= Combined Items and Notifications ============================

    @Transaction
    @Query("Select * from items where id = :id")
    public abstract LiveData<ItemAndNotifications> getItemAndNotifications(long id);

    @Transaction
    @Query("Select * from items")
    public abstract LiveData<List<ItemAndNotifications>> getItemAndNotificationsList();
}
