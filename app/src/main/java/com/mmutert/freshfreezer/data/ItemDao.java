package com.mmutert.freshfreezer.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public abstract class ItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract long insertItem(FrozenItem item);

    @Query("SELECT * FROM items")
    public abstract LiveData<List<FrozenItem>> getAllItems();

    @Query("SELECT * FROM items WHERE name = :name")
    public abstract LiveData<List<FrozenItem>> getAllItemsWithName(String name);

    @Query("SELECT * FROM items ORDER BY best_before_date ASC LIMIT :numResults")
    public abstract LiveData<List<FrozenItem>> getClosestBestBefore(int numResults);

    @Delete
    public abstract void deleteItem(FrozenItem item);
}
