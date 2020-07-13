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
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;
import java.util.Locale;
import java.util.UUID;


public class FrozenItemViewModel extends AndroidViewModel {

    private ItemRepository mItemRepository;

    private LiveData<List<FrozenItem>> mFrozenItems;

    private SortingOption.SortingOrder sortingOrder = SortingOption.SortingOrder.ASCENDING;
    private SortingOption sortingOption = SortingOption.DATE_BEST_BEFORE;

   public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.longDate().withLocale(Locale.getDefault());


    public FrozenItemViewModel(@NonNull Application application) {
        super(application);
        mItemRepository = new ItemRepository(application);
        mFrozenItems = mItemRepository.getAllActiveFrozenItems();
    }


    public LiveData<List<FrozenItem>> getFrozenItems() {
        return mFrozenItems;
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

    /*
     * Notifications
     */

    /**
     * Get all scheduled notifications for the given item
     * @param item The item to retrieve the scheduled notifications for.
     * @return The list of notifications for the item.
     */
    public List<ItemNotification> getAllNotifications(FrozenItem item) {
        return mItemRepository.getAllNotifications(item);
    }
}
