package com.mmutert.freshfreezer.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.work.WorkManager;

import com.mmutert.freshfreezer.data.FrozenItem;
import com.mmutert.freshfreezer.data.ItemNotification;
import com.mmutert.freshfreezer.data.ItemRepository;
import com.mmutert.freshfreezer.util.SortingOption;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ItemListViewModel extends AndroidViewModel {

    public static final String TAG = "FrozenItemViewModel";

    private ItemRepository mItemRepository;

    private LiveData<List<FrozenItem>> mFrozenItems;

    private SortingOption.SortingOrder sortingOrder = SortingOption.SortingOrder.ASCENDING;
    private SortingOption sortingOption = SortingOption.DATE_BEST_BEFORE;


    public ItemListViewModel(@NonNull Application application) {
        super(application);
        mItemRepository = new ItemRepository(application);
        mFrozenItems = mItemRepository.getAllActiveFrozenItems();
    }


    public LiveData<List<FrozenItem>> getFrozenItems() {
        return mFrozenItems;
    }

    public void updateItem(FrozenItem item) {
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

    /**
     * Cancels all notification workers for the given item.
     * @param itemToArchive The item for which the notification workers should be cancelled.
     */
    public void cancelNotifications(final FrozenItem itemToArchive) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<ItemNotification> allNotifications = getAllNotifications(itemToArchive);

            // Cancel all notifications
            WorkManager workManager = WorkManager.getInstance(getApplication());
            for (ItemNotification notification : allNotifications) {
                workManager.cancelWorkById(notification.getNotificationId());
                Log.d(TAG, "Cancelled the notification worker with uuid: " + notification.getNotificationId());
            }
        });
    }

    /**
     * Take the given amount from the given model and update the item in the repository
     * @param item The item to take from
     * @param amountTaken The amount that was taken from the item
     */
    public void takeFromItem(final FrozenItem item, final float amountTaken) {
        float newAmount = Math.max(0.0F, item.getAmount() - amountTaken);
        // TODO Check for possible side effects and possibly create copy of item first
        item.setAmount(newAmount);
        updateItem(item);
    }
}
