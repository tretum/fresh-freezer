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
import org.joda.time.Period;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


public class NotificationHelper {

    public static final String TAG = NotificationHelper.class.getName();

    public static UUID scheduleNotification(
            @NonNull Context context,
            FrozenItem item,
            TimeUnit timeUnit,
            LocalDateTime scheduledOn) {

        Log.d(TAG, "Scheduling Notification for item " + item.getName());
        Log.d(TAG, "Scheduled date is " + scheduledOn.toString());

        long offset = calculateOffset(timeUnit, LocalDateTime.now().toDate(), scheduledOn.toDate());

        return scheduleNotification(context, item, offset, timeUnit);
    }

    public static UUID scheduleNotification(@NonNull Context context, FrozenItem item, long timeOffset, TimeUnit timeUnit) {
        Log.d(TAG, "Creating workrequest for item " + item.getName());
        Log.d(TAG, "Offset is " + timeOffset + " and unit " + timeUnit.name());
        OneTimeWorkRequest notificationRequest =
                new OneTimeWorkRequest.Builder(NotificationWorker.class)
                        .setInputData(NotificationHelper.createInputDataForItem(item))
                        .setConstraints(
                                new Constraints.Builder()
                                        .setRequiresBatteryNotLow(true)
                                        .build())
                        .setInitialDelay(
                                timeOffset,
                                timeUnit
                        )
                        .build();

        WorkManager.getInstance(context).enqueue(notificationRequest);
        return notificationRequest.getId();
    }

    public static Data createInputDataForItem(FrozenItem item) {
        return new Data.Builder()
                .putString(NotificationConstants.KEY_ITEM_NAME, item.getName())
                .putFloat(NotificationConstants.KEY_ITEM_AMOUNT, item.getAmount())
                .putInt(NotificationConstants.KEY_ITEM_ID, (int) item.getId())
                .putString(NotificationConstants.KEY_ITEM_AMOUNT_UNIT, item.getUnit().toString())
                .build();
    }

    private static long calculateOffset(TimeUnit timeUnit, Date goalDate, Date startDate) {
        long goalDateInMillis = goalDate.getTime();
        long startDateInMillis = startDate.getTime();
        return timeUnit.convert(
                Math.abs(goalDateInMillis - startDateInMillis),
                TimeUnit.MILLISECONDS
        );
    }

}
