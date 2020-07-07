package com.mmutert.freshfreezer.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.mmutert.freshfreezer.data.FrozenItem;
import com.mmutert.freshfreezer.data.ItemNotification;
import com.mmutert.freshfreezer.data.ItemRepository;
import com.mmutert.freshfreezer.util.SortingOption;

import org.joda.time.LocalDateTime;

import java.util.List;
import java.util.UUID;


public class FrozenItemViewModel extends AndroidViewModel {

    private ItemRepository mItemRepository;

    private LiveData<List<FrozenItem>> mFrozenItems;

    private SortingOption.SortingOrder sortingOrder = SortingOption.SortingOrder.DESCENDING;
    private SortingOption sortingOption = SortingOption.NAME;

    public FrozenItemViewModel(@NonNull Application application) {
        super(application);
        mItemRepository = new ItemRepository(application);
        mFrozenItems = mItemRepository.getAllActiveFrozenItems();
    }


    public LiveData<List<FrozenItem>> getFrozenItems() {
        return mFrozenItems;
    }

    public void insert(FrozenItem item) {
        mItemRepository.insertItem(item);
    }

    public void updateItem(FrozenItem item, float newAmount) {
        // TODO Check for possible side effects and possibly create copy of item first
        item.setAmount(newAmount);
        mItemRepository.updateItem(item);
    }

    public void delete(FrozenItem item) {
        mItemRepository.deleteItem(item);
    }

    public void archive(FrozenItem item) {
        mItemRepository.archiveItem(item);
    }

    public void restore(FrozenItem item) {
        mItemRepository.restoreItem(item);
    }

    public void addNotification(FrozenItem item, UUID uuid, LocalDateTime notifyOn){
        ItemNotification notification = new ItemNotification(uuid, item.getId(), notifyOn);
        mItemRepository.addNotification(notification);
    }

    public LiveData<List<ItemNotification>> getAllNotificationsLiveData(FrozenItem item) {
        return mItemRepository.getAllNotificationsLiveData(item);
    }
    public List<ItemNotification> getAllNotifications(FrozenItem item) {
        return mItemRepository.getAllNotifications(item);
    }

    public void deleteNotification(ItemNotification notification) {
        mItemRepository.deleteNotification(notification);
    }


    public SortingOption.SortingOrder getSortingOrder() {
        return sortingOrder;
    }

    public void setSortingOrder(final SortingOption.SortingOrder sortingOrder) {
        this.sortingOrder = sortingOrder;
    }

    public SortingOption getSortingOption() {
        return sortingOption;
    }

    public void setSortingOption(final SortingOption sortingOption) {
        this.sortingOption = sortingOption;
    }
}
