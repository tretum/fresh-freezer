package com.mmutert.freshfreezer.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.mmutert.freshfreezer.data.FrozenItem;
import com.mmutert.freshfreezer.data.ItemRepository;

import java.util.List;

public class FrozenItemViewModel extends AndroidViewModel {

    private ItemRepository mItemRepository;

    private LiveData<List<FrozenItem>> mFrozenItems;

    public FrozenItemViewModel(@NonNull Application application) {
        super(application);
        mItemRepository = new ItemRepository(application);
        mFrozenItems = mItemRepository.getAllFrozenItems();
    }


    public LiveData<List<FrozenItem>> getFrozenItems() {
        return mFrozenItems;
    }

    public void insert(FrozenItem item) {
        mItemRepository.insertItem(item);
    }

    public void delete(FrozenItem item) {
        mItemRepository.deleteItem(item);
    }
}
