package com.mmutert.freshfreezer.notification;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.mmutert.freshfreezer.data.FrozenItem;

import org.joda.time.LocalDateTime;

import java.util.UUID;
import java.util.concurrent.TimeUnit;


public class NotificationHelper {

    public static final String TAG = NotificationHelper.class.getName();

    /**
     * Creates a work request for the notification of the given item.
     *
     * @param context     Context used to get the {@link WorkManager} instance
     * @param item        The item to schedule a notification for
     * @param timeUnit    The time unit for the offset
     * @param scheduledOn The time at which the notification should be scheduled.
     * @return The id of the work request
     */
    public static UUID scheduleNotification(
            @NonNull Context context,
            FrozenItem item,
            TimeUnit timeUnit,
            LocalDateTime scheduledOn) {

        Log.d(TAG, "Scheduling Notification for item " + item.getName());
        Log.d(TAG, "Scheduled date is " + scheduledOn.toString());

        LocalDateTime startDate = LocalDateTime.now();
        long offset = calculateOffset(timeUnit, startDate, scheduledOn);

        return scheduleNotification(context, item, offset, timeUnit);
    }

    /**
     * Creates a work request for the notification of the given item.
     *
     * @param context    Context used to get the {@link WorkManager} instance
     * @param item       The item to schedule a notification for
     * @param timeOffset The offset at which to display the notification
     * @param timeUnit   The time unit for the offset
     * @return The id of the work request
     */
    public static UUID scheduleNotification(
            @NonNull Context context,
            FrozenItem item,
            long timeOffset,
            TimeUnit timeUnit) {
        Log.d(TAG, "Creating WorkRequest for item " + item.getName());
        Log.d(TAG, "Offset is " + timeOffset + " and unit " + timeUnit.name());
        OneTimeWorkRequest notificationRequest =
                new OneTimeWorkRequest.Builder(NotificationWorker.class)
                        .setInputData(NotificationHelper.createInputDataForItem(item))
                        .setConstraints(
                                new Constraints.Builder()
                                        .setRequiresBatteryNotLow(true)
                                        .build())
                        .setInitialDelay(timeOffset, timeUnit)
                        .build();

        WorkManager.getInstance(context).enqueue(notificationRequest);
        return notificationRequest.getId();
    }

    /**
     * Creates the input data for the Notification Worker that creates the notification.
     *
     * @param item The item to create the data for
     * @return The created data object.
     */
    private static Data createInputDataForItem(FrozenItem item) {
        return new Data.Builder()
                .putString(NotificationConstants.KEY_ITEM_NAME, item.getName())
                .putFloat(NotificationConstants.KEY_ITEM_AMOUNT, item.getAmount())
                .putInt(NotificationConstants.KEY_ITEM_ID, (int) item.getId())
                .putString(NotificationConstants.KEY_ITEM_AMOUNT_UNIT, item.getUnit().toString())
                .build();
    }

    /**
     * Calculates the offset
     *
     * @param timeUnit  The time unit for the calculated offset
     * @param startDate The date to which the offset should be added in order to get to the goal date.
     * @param goalDate  The end date that should be reached by adding the offset to the start date
     * @return The calculated offset
     */
    static long calculateOffset(TimeUnit timeUnit, LocalDateTime startDate, LocalDateTime goalDate) {
        long goalDateInMillis = goalDate.toDate().getTime();
        long startDateInMillis = startDate.toDate().getTime();
        return timeUnit.convert(
                Math.abs(goalDateInMillis - startDateInMillis),
                TimeUnit.MILLISECONDS
        );
    }

}
