package com.mmutert.freshfreezer.notification;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.mmutert.freshfreezer.R;
import com.mmutert.freshfreezer.data.FrozenItem;
import com.mmutert.freshfreezer.data.ItemNotification;
import com.mmutert.freshfreezer.data.TimeOffsetUnit;
import com.mmutert.freshfreezer.util.TimeHelper;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;

import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.mmutert.freshfreezer.notification.NotificationConstants.NOTIFICATION_OFFSET_TIMEUNIT;


public class NotificationHelper {

    public static final String TAG = "NotificationHelper";


    /**
     * Creates a work request for the notification of the given item.
     *
     * @param context Context used to get the {@link WorkManager} instance
     * @param item    The item to schedule a notification for
     * @return The id of the work request
     */
    public static UUID scheduleNotification(
            @NonNull Context context,
            FrozenItem item,
            ItemNotification notification) {

        LocalTime notificationTime = LocalTime.now(DateTimeZone.forTimeZone(TimeZone.getDefault()));
        // TODO Set notification time to the one saved in the pending notification object, otherwise used preference

        LocalDateTime goalDateTime = determineGoalDateTime(
                item,
                notification.getTimeOffsetUnit(),
                notification.getOffsetAmount(),
                notificationTime
        );

        // Precondition: Notification has to be scheduled after current date
        if (TimeHelper.getCurrentDateTimeLocalized().isAfter(goalDateTime)) {
            String name = item.getName() != null && !item.getName().isEmpty()
                    ? item.getName()
                    : context.getString(R.string.empty_name_placeholder);
            Log.e(
                    TAG,
                    "Could not schedule notification for item " + name
                            + ". The scheduled time "
                            + DateTimeFormat.fullDate().print(goalDateTime) +
                            " is in the past. Current time is "
                            + DateTimeFormat.fullDate().print(TimeHelper.getCurrentDateTimeLocalized())
            );

            return null;
        }

        LocalDateTime startDate = TimeHelper.getCurrentDateTimeLocalized();
        long offset = calculateOffset(NOTIFICATION_OFFSET_TIMEUNIT, startDate, goalDateTime);

        Data inputData = createInputDataForItem(context, item, notification);

        OneTimeWorkRequest notificationRequest = createWorkRequest(inputData, offset, NOTIFICATION_OFFSET_TIMEUNIT);
        WorkManager.getInstance(context).enqueue(notificationRequest);

        Log.d(TAG, "Enqueued the notification worker with uuid: " + notificationRequest.getId());
        return notificationRequest.getId();
    }


    /**
     * Creates a work request for the notification of the given item with the given offset from the current time
     *
     * @param timeOffset The offset from the current time at which the notification should be displayed
     * @param timeUnit   The unit for the offset.
     * @return The created {@link androidx.work.WorkRequest}
     */
    public static OneTimeWorkRequest createWorkRequest(Data inputData, long timeOffset, TimeUnit timeUnit) {

        return new OneTimeWorkRequest.Builder(NotificationWorker.class)
                .setInputData(inputData)
                .setConstraints(
                        new Constraints.Builder()
                                .setRequiresBatteryNotLow(true)
                                .build())
                .setInitialDelay(timeOffset, timeUnit)
                .build();
    }


    /**
     * Creates the input data for the Notification Worker that creates the notification.
     *
     * @param item The item to create the data for
     * @return The created data object.
     */
    public static Data createInputDataForItem(Context context, FrozenItem item, ItemNotification notification) {

        TimeOffsetUnit timeOffsetUnit = notification.getTimeOffsetUnit();
        int offsetAmount = notification.getOffsetAmount();

        String offsetUnitFormatted = "";
        switch (timeOffsetUnit) {
            case DAYS:
                offsetUnitFormatted = context.getResources().getQuantityString(
                        R.plurals.days_capitalized,
                        offsetAmount,
                        offsetAmount
                );
                break;
            case WEEKS:
                offsetUnitFormatted = context.getResources().getQuantityString(
                        R.plurals.weeks_capitalized,
                        offsetAmount,
                        offsetAmount
                );
                break;
            case MONTHS:
                offsetUnitFormatted = context.getResources().getQuantityString(
                        R.plurals.months_capitalized,
                        offsetAmount,
                        offsetAmount
                );
                break;
        }

        String name = item.getName() != null && !item.getName().isEmpty()
                ? item.getName()
                : context.getString(R.string.empty_name_placeholder);

        return new Data.Builder()
                .putString(NotificationConstants.KEY_ITEM_NAME, name)
                .putFloat(NotificationConstants.KEY_ITEM_AMOUNT, item.getAmount())
                .putInt(NotificationConstants.KEY_ITEM_ID, (int) item.getId())
                .putString(NotificationConstants.KEY_ITEM_AMOUNT_UNIT, item.getUnit().toString())
                .putString(
                        NotificationConstants.KEY_ITEM_BEST_BEFORE_DATE,
                        DateTimeFormat.longDate().withLocale(Locale.getDefault()).print(item.getBestBeforeDate())
                )
                .putString(
                        NotificationConstants.KEY_NOTIFICATION_OFFSET_AMOUNT,
                        String.valueOf(notification.getOffsetAmount())
                )
                .putString(NotificationConstants.KEY_NOTIFICATION_OFFSET_UNIT, offsetUnitFormatted)
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


    private static LocalDateTime determineGoalDateTime(
            FrozenItem item,
            final TimeOffsetUnit timeUnit,
            final int offsetAmount,
            final LocalTime notificationTime) {

        // TODO Fix: Currently one day off
        LocalDateTime goalDateTime = item.getBestBeforeDate().toLocalDateTime(notificationTime);

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

}
