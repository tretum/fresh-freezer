package com.mmutert.freshfreezer.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.work.Operation;
import androidx.work.WorkManager;

import com.mmutert.freshfreezer.data.AmountUnit;
import com.mmutert.freshfreezer.data.FrozenItem;
import com.mmutert.freshfreezer.data.ItemAndNotifications;
import com.mmutert.freshfreezer.data.ItemNotification;
import com.mmutert.freshfreezer.data.ItemRepository;
import com.mmutert.freshfreezer.data.TimeOffsetUnit;
import com.mmutert.freshfreezer.notification.NotificationHelper;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.Executors;

import static com.mmutert.freshfreezer.notification.NotificationConstants.NOTIFICATION_OFFSET_TIMEUNIT;


public class AddItemViewModel extends AndroidViewModel {

    public static final String TAG = "AddItemViewModel";
    public final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.longDate().withLocale(Locale.getDefault());

    private FrozenItem currentItem;
    private boolean editing = false;

    private ItemRepository mItemRepository;
    private List<ItemNotification> notifications;
    private List<ItemNotification> notificationsToDelete;


    public AddItemViewModel(@NonNull final Application application) {

        super(application);
        mItemRepository       = new ItemRepository(application);
        currentItem           = new FrozenItem();
        notificationsToDelete = new ArrayList<>();
        notifications         = new ArrayList<>();
    }


    /**
     * Resets this ViewModel to the "New Item" state, i.e. not editing another item.
     */
    public void reset() {

        currentItem           = new FrozenItem();
        notificationsToDelete = new ArrayList<>();
        notifications         = new ArrayList<>();
        editing               = false;
        // TODO Notifications
    }


    /**
     * Mark a notification as being deleted on the UI and to be permanently removed on saving the item.
     *
     * @param notification The notification to mark as deleted.
     */
    public void addNotificationToDelete(ItemNotification notification) {

        Log.d(TAG,
              "Adding notification " + notification.getOffsetAmount() + notification.getTimeOffsetUnit()
                      + " to the delete list."
        );
        this.notificationsToDelete.add(notification);
        this.notifications.remove(notification);
    }


    /**
     * Cancels notifications pending deletion and removes them from the repository
     */
    public void removeNotificationsToDelete() {

        for (ItemNotification notification : notificationsToDelete) {
            // Remove notification workers
            if (notification.getNotificationId() != null) {
                WorkManager workManager = WorkManager
                        .getInstance(getApplication());
                Operation operation = workManager
                        .cancelWorkById(notification.getNotificationId());

                operation.getResult().addListener(() -> {
                    Log.d(TAG, "Cancelled the notification worker with uuid: " + notification.getNotificationId());
                    Log.d(TAG, "For notification with offset " + notification.getTimeOffsetUnit().toString()
                            + " and amount " + notification.getOffsetAmount()
                    );
                }, Executors.newSingleThreadExecutor());

            }

            // Remove from repository
            deleteNotificationFromRepository(notification);
        }
    }


    public void deleteNotificationFromRepository(ItemNotification notification) {

        Log.d(TAG,
              "Deleting notification from repository. " + notification.getOffsetAmount()
                      + notification.getTimeOffsetUnit() + " " + notification.getNotificationId()
        );
        mItemRepository.deleteNotification(notification);
    }


    /**
     * Schedules all non-scheduled notifications.
     */
    public void scheduleNotifications() {

        // TODO Check for possible race conditions where id for the item might not be set yet
        for (ItemNotification notification : notifications) {

            if (notification.getNotificationId() == null) {
                // Notification needs to be scheduled

                LocalTime notificationTime = LocalTime.now(DateTimeZone.forTimeZone(TimeZone.getDefault()));
                // TODO Set notification time to the one saved in the pending notification object, otherwise used preference

                LocalDateTime goalDateTime = determineGoalDateTime(
                        notification.getTimeOffsetUnit(),
                        notification.getOffsetAmount(),
                        notificationTime
                );

                if (!LocalDateTime.now().isAfter(goalDateTime)) {
                    UUID uuid = NotificationHelper.scheduleNotification(
                            getApplication(),
                            currentItem,
                            NOTIFICATION_OFFSET_TIMEUNIT,
                            goalDateTime
                    );

                    notification.setNotificationId(uuid);
                    notification.setItemId(currentItem.getId());
                    mItemRepository.addNotification(notification);

                    Log.d(TAG, "Scheduled a notification with UUID: " + uuid);
                } else {
                    Log.e(
                            TAG,
                            "Could not schedule notification for item " + currentItem.getName()
                                    + ". The scheduled time "
                                    + DateTimeFormat.fullDate().print(goalDateTime) +
                                    " is in the past. Current time is "
                                    + DateTimeFormat.fullDate().print(LocalDateTime.now())
                    );
                }

            } else {
                // Notification got scheduled before
            }
        }
    }


    private LocalDateTime determineGoalDateTime(
            final TimeOffsetUnit timeUnit,
            final int offsetAmount,
            final LocalTime notificationTime) {

        // TODO Fix: Currently one day off
        LocalDateTime goalDateTime = currentItem.getBestBeforeDate().toLocalDateTime(notificationTime);

        switch (timeUnit) {
            case DAYS:
                goalDateTime = goalDateTime.minusDays(offsetAmount);
                break;
            case WEEKS:
                goalDateTime = goalDateTime.minusWeeks(offsetAmount);
                break;
            case MONTHS:
                goalDateTime = goalDateTime.minusMonths(offsetAmount);
                break;
        }
        return goalDateTime;
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

        this.currentItem      = item;
        this.editing          = true;
        notificationsToDelete = new ArrayList<>();
    }


    public void setCurrentNotifications(final List<ItemNotification> notifications) {

        this.notifications = notifications;
    }


    public void save() {

        // If the creation date was not set before, i.e. the item not edited, set the date
        if (currentItem.getItemCreationDate() == null) {
            currentItem.setItemCreationDate(LocalDateTime.now());
        }

        // Always update the last changed at date
        currentItem.setLastChangedAtDate(LocalDateTime.now());
        mItemRepository.insertItem(currentItem);

        // TODO Check for weird LiveData updates and possible problems with scheduling and deleting
        //  Might need switching these instructions around or possibly more effort
        removeNotificationsToDelete();
        scheduleNotifications();
    }


    public ItemNotification addNotification(final int enteredOffset, final TimeOffsetUnit offSetUnitTime) {

        ItemNotification itemNotification = new ItemNotification(null, -1, offSetUnitTime, enteredOffset);
        this.notifications.add(itemNotification);
        return itemNotification;
    }


    public List<ItemNotification> getCurrentNotifications() {

        return notifications;
    }


    /**
     * Remember to update the list in the recyclerview after calling this method since the list in the RV is immutable and needs to be updated
     *
     * @param date
     */
    public void updateBestBefore(final LocalDate date) {

        // Precondition: Date should have changed
        if (currentItem.getBestBeforeDate() != null && currentItem.getBestBeforeDate().isEqual(date)) {
            // Noop
            return;
        }

        currentItem.setBestBeforeDate(date);

        ArrayList<ItemNotification> newNotifications = new ArrayList<>();

        if (editing) {
            for (ItemNotification notification : notifications) {
                ItemNotification notificationCopy = new ItemNotification(
                        null,
                        currentItem.getId(),
                        notification.getTimeOffsetUnit(),
                        notification.getOffsetAmount()
                );

                // TODO Check if the delete thing should be here
                notificationsToDelete.add(notification);
                Log.d(
                        TAG,
                        "Marking notification " + notification.getOffsetAmount() + " " + notification
                                .getTimeOffsetUnit()
                                .toString() + " as to delete due to best before date change."
                );

                newNotifications.add(notificationCopy);
            }
            notifications = newNotifications;
        }
    }
}
