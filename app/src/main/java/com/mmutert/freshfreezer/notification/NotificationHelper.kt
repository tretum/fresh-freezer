package com.mmutert.freshfreezer.notification

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.mmutert.freshfreezer.R
import com.mmutert.freshfreezer.data.ItemNotification
import com.mmutert.freshfreezer.data.StorageItem
import com.mmutert.freshfreezer.data.TimeOffsetUnit
import com.mmutert.freshfreezer.util.TimeHelper
import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

object NotificationHelper {
    private const val TAG = "NotificationHelper"

    /**
     * Creates a work request for the notification of the given item.
     *
     * @param context Context used to get the [WorkManager] instance
     * @param item    The item to schedule a notification for
     * @param notification
     * @return The id of the work request
     */
    fun scheduleNotification(
            context: Context,
            item: StorageItem,
            notification: ItemNotification
    ): UUID? {

        // TODO Set notification time to the one saved in the pending notification object, otherwise used preference
        // Determine the date and time at which the notification should be shown to the user
        val notificationTime = LocalTime.now(DateTimeZone.forTimeZone(TimeZone.getDefault()))
        val goalDateTime = determineGoalDateTime(
            item,
            notification.timeOffsetUnit,
            notification.offsetAmount,
            notificationTime
        )

        // Precondition: Notification should be shown after the current time and date
        if (TimeHelper.currentDateTimeLocalized.isAfter(goalDateTime)) {
            val name =
                    if (item.name.isNotEmpty()) item.name else context.getString(R.string.empty_name_placeholder)
            val formatter = DateTimeFormat.fullDate()
            Log.e(
                TAG,
                "Could not schedule notification for item $name. " +
                        "The scheduled time ${formatter.print(goalDateTime)} is in the past. " +
                        "Current time is ${formatter.print(TimeHelper.currentDateTimeLocalized)}"
            )
            return null
        }

        // Actual scheduling of the notification
        val startDate = TimeHelper.currentDateTimeLocalized
        val convertedWorkOffset = calculateOffset(
            NotificationConstants.NOTIFICATION_OFFSET_TIME_UNIT, startDate, goalDateTime
        )
        val inputData = createInputDataForItem(context, item, notification)
        val notificationRequest = createWorkRequest(
            inputData,
            convertedWorkOffset,
            NotificationConstants.NOTIFICATION_OFFSET_TIME_UNIT
        )
        WorkManager.getInstance(context).enqueue(notificationRequest)
        Log.d(TAG, "Enqueued the notification worker with uuid: " + notificationRequest.id)
        return notificationRequest.id
    }

    /**
     * Creates a work request for the notification of the given item with the given offset from the current time
     *
     * @param timeOffset The offset from the current time at which the notification should be displayed
     * @param timeUnit   The unit for the offset.
     * @return The created [androidx.work.WorkRequest]
     */
    private fun createWorkRequest(inputData: Data,
                                  timeOffset: Long,
                                  timeUnit: TimeUnit): OneTimeWorkRequest {
        return OneTimeWorkRequest.Builder(NotificationWorker::class.java)
            .setInputData(inputData)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build())
            .setInitialDelay(timeOffset, timeUnit)
            .build()
    }

    /**
     * Creates the input data for the Notification Worker that creates the notification.
     *
     * @param context
     * @param item The item to create the data for
     * @param notification
     * @return The created data object.
     */
    fun createInputDataForItem(
            context: Context,
            item: StorageItem,
            notification: ItemNotification
    ): Data {
        val offsetAmount = notification.offsetAmount
        val offsetUnitFormatted = when (notification.timeOffsetUnit) {
            TimeOffsetUnit.DAYS -> context.resources.getQuantityString(
                R.plurals.days_capitalized,
                offsetAmount,
                offsetAmount
            )
            TimeOffsetUnit.WEEKS -> context.resources.getQuantityString(
                R.plurals.weeks_capitalized,
                offsetAmount,
                offsetAmount
            )
            TimeOffsetUnit.MONTHS -> context.resources.getQuantityString(
                R.plurals.months_capitalized,
                offsetAmount,
                offsetAmount
            )
        }

        val name =
                if (item.name.isNotEmpty()) item.name else context.getString(R.string.empty_name_placeholder)

        return Data.Builder()
            .putString(NotificationConstants.KEY_ITEM_NAME, name)
            .putFloat(NotificationConstants.KEY_ITEM_AMOUNT, item.amount)
            .putLong(NotificationConstants.KEY_ITEM_ID, item.id)
            .putString(NotificationConstants.KEY_ITEM_AMOUNT_UNIT, item.unit.toString())
            .putString(
                NotificationConstants.KEY_ITEM_BEST_BEFORE_DATE,
                DateTimeFormat.longDate().withLocale(Locale.getDefault()).print(item.bestBeforeDate)
            )
            .putString(
                NotificationConstants.KEY_NOTIFICATION_OFFSET_AMOUNT,
                notification.offsetAmount.toString())
            .putString(NotificationConstants.KEY_NOTIFICATION_OFFSET_UNIT, offsetUnitFormatted)
            .build()
    }

    /**
     * Calculates the offset
     *
     * @param timeUnit  The time unit for the calculated offset
     * @param startDate The date to which the offset should be added in order to get to the goal date.
     * @param goalDate  The end date that should be reached by adding the offset to the start date
     * @return The calculated offset
     */
    fun calculateOffset(timeUnit: TimeUnit,
                        startDate: LocalDateTime,
                        goalDate: LocalDateTime
    ): Long {
        val goalDateInMillis = goalDate.toDate().time
        val startDateInMillis = startDate.toDate().time
        return timeUnit.convert(
            abs(goalDateInMillis - startDateInMillis),
            TimeUnit.MILLISECONDS
        )
    }

    /**
     * Calculates the date time that is the result of applying the notification offset to the best before date of the given [item].
     * Furthermore, the given [notificationTime] is used to set the correct time of the notification.
     *
     * @param item The item for which the notification time should be determined
     * @param timeUnit The unit of the notification offset
     * @param offsetAmount The amount of time offset in the given [timeUnit]
     * @param notificationTime The actual time for the notification at the calculated notification date
     */
    private fun determineGoalDateTime(
            item: StorageItem,
            timeUnit: TimeOffsetUnit,
            offsetAmount: Int,
            notificationTime: LocalTime
    ): LocalDateTime {

        // TODO Check if still one day off
        val bestBeforeDateTime = item.bestBeforeDate.toLocalDateTime(notificationTime)
        return when (timeUnit) {
            TimeOffsetUnit.DAYS -> bestBeforeDateTime.minusDays(offsetAmount)
            TimeOffsetUnit.WEEKS -> bestBeforeDateTime.minusWeeks(offsetAmount)
            TimeOffsetUnit.MONTHS -> bestBeforeDateTime.minusMonths(offsetAmount)
        }
    }
}