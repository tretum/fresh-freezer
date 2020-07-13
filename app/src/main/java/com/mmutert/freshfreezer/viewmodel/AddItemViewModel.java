package com.mmutert.freshfreezer.viewmodel;

import android.app.Application;
import android.app.Notification;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.mmutert.freshfreezer.data.AmountUnit;
import com.mmutert.freshfreezer.data.FrozenItem;
import com.mmutert.freshfreezer.data.ItemNotification;
import com.mmutert.freshfreezer.data.ItemRepository;
import com.mmutert.freshfreezer.data.converters.ItemAndNotifications;
import com.mmutert.freshfreezer.notification.NotificationHelper;
import com.mmutert.freshfreezer.ui.PendingNotification;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.Executors;

import static com.mmutert.freshfreezer.notification.NotificationConstants.NOTIFICATION_OFFSET_TIMEUNIT;


public class AddItemViewModel extends AndroidViewModel {

    public AddItemViewModel(@NonNull final Application application) {
        super(application);
        mItemRepository = new ItemRepository(application);
        currentItem     = new FrozenItem();
    }

    private FrozenItem currentItem;
    private List<PendingNotification> pendingNotifications = new ArrayList<>();
    public static final String TAG = AddItemViewModel.class.getName();

    private ItemRepository mItemRepository;

    public void newItem() {
        currentItem = new FrozenItem();
    }

    /**
     * Sets missing non-null fields of the given item and inserts the item into the repository.
     */
    public void insertItem() {
        currentItem.setItemCreationDate(LocalDateTime.now());
        currentItem.setLastChangedAtDate(LocalDateTime.now());
        mItemRepository.insertItem(currentItem);
    }

    /**
     * Add a notification for the given item to the repository.
     * @param uuid The UUID of the worker that is used to schedule the notification
     * @param notifyOn The exact time the notification is scheduled on.
     */
    public void addNotification(UUID uuid, LocalDateTime notifyOn){
        ItemNotification notification = new ItemNotification(uuid, currentItem.getId(), notifyOn);
        mItemRepository.addNotification(notification);
    }

    public LiveData<List<ItemNotification>> getAllNotificationsLiveData() {
        return mItemRepository.getAllNotificationsLiveData(currentItem);
    }

    /**
     * Get all scheduled notifications for the given item
     * @return The list of notifications for the item.
     */
    public List<ItemNotification> getAllNotifications() {
        return mItemRepository.getAllNotifications(currentItem);
    }

    public void deleteNotification(ItemNotification notification) {
        mItemRepository.deleteNotification(notification);
    }

    public void scheduleNotifications() {
        // TODO Check for possible race conditions where id for the item might not be set yet
        for (PendingNotification notification : pendingNotifications) {
            Log.d(TAG, "Scheduling notification");

            LocalTime notificationTime = LocalTime.now(DateTimeZone.forTimeZone(TimeZone.getDefault()));
            // TODO Set notification time to the one saved in the pending notification object, otherwise used preference

            LocalDateTime scheduledOn = currentItem
                    .getBestBeforeDate()
                    .toLocalDateTime(notificationTime);

            switch (notification.getTimeUnit()) {
                case DAYS:
                    scheduledOn = scheduledOn.minusDays(notification.getOffsetAmount());
                    break;
                case WEEKS:
                    scheduledOn = scheduledOn.minusWeeks(notification.getOffsetAmount());
                    break;
                case MONTHS:
                    scheduledOn = scheduledOn.minusMonths(notification.getOffsetAmount());
                    break;
            }

            if (!LocalDateTime.now().isAfter(scheduledOn)) {
                UUID uuid = NotificationHelper.scheduleNotification(
                        getApplication(),
                        currentItem,
                        NOTIFICATION_OFFSET_TIMEUNIT,
                        scheduledOn
                );
                this.addNotification(uuid, scheduledOn);
            } else {
                Log.d(
                        TAG,
                        "Could not schedule notification for item " + currentItem.getName()
                                + ". The scheduled time "
                                + DateTimeFormat.fullDate().print(scheduledOn) +
                                " is in the past. Current time is "
                                + DateTimeFormat.fullDate().print(LocalDateTime.now())
                );
            }
        }
    }



    public void addPendingNotification(final PendingNotification notification) {
        this.pendingNotifications.add(notification);
    }

    public void removePendingNotification(final PendingNotification notification) {
        this.pendingNotifications.remove(notification);
    }

    public FrozenItem getItem() {
        return currentItem;
    }

    public void setUnit(final AmountUnit selectedUnit) {
        currentItem.setUnit(selectedUnit);
    }

    public LiveData<ItemAndNotifications> getItemAndNotifications(long itemId) {
        return mItemRepository.getItemAndNotificationsLiveData(itemId);
    }

    public void setCurrentItem(FrozenItem item) {
        this.currentItem = item;
    }
}
