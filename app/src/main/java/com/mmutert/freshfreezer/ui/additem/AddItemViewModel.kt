package com.mmutert.freshfreezer.ui.additem

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.mmutert.freshfreezer.data.*
import com.mmutert.freshfreezer.data.ItemDatabase.Companion.getDatabase
import com.mmutert.freshfreezer.notification.NotificationHelper.scheduleNotification
import com.mmutert.freshfreezer.util.TimeHelper
import kotlinx.coroutines.launch
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.Executors

class AddItemViewModel(application: Application) : AndroidViewModel(application) {

    @JvmField
    val DATE_FORMATTER: DateTimeFormatter =
            DateTimeFormat.longDate().withLocale(Locale.getDefault())

    var currentItem: FrozenItem = createNewItem()
        set(value) {
            field = value
            editing = true
            notificationsToDelete = ArrayList()
        }
    var notifications: MutableList<ItemNotification> = ArrayList()

    private var editing = false
    private val mItemRepository: ItemRepository = ItemRepository(getDatabase(application).itemDao())
    private var notificationsToDelete: MutableList<ItemNotification> = ArrayList()

    /**
     * Resets this ViewModel to the "New Item" state, i.e. not editing another item.
     */
    fun reset() {
        currentItem = createNewItem()
        notificationsToDelete = ArrayList()
        notifications = ArrayList()
        editing = false
        // TODO Notifications
    }

    private fun createNewItem(): FrozenItem {
        val currentDate = LocalDate.now(DateTimeZone.getDefault())
        val currentDateTime = TimeHelper.currentDateTimeLocalized
        return FrozenItem(
            0,
            "",
            0f,
            AmountUnit.GRAMS,
            null,
            currentDate,
            currentDateTime,
            currentDateTime,
            null,
            Condition.ROOM_TEMP,
            false
        )
    }

    /**
     * Mark a notification as being deleted on the UI and to be permanently removed on saving the item.
     *
     * @param notification The notification to mark as deleted.
     */
    fun addNotificationToDelete(notification: ItemNotification) {
        Log.d(
            TAG,
            "Adding notification " + notification.offsetAmount + notification.timeOffsetUnit
                    + " to the delete list."
        )
        notificationsToDelete.add(notification)
        notifications.remove(notification)
    }

    /**
     * Cancels notifications pending deletion and removes them from the repository
     */
    fun removeNotificationsToDelete() {
        for (notification in notificationsToDelete) {
            // Remove notification workers
            if (notification.notificationId != null) {
                val workManager = WorkManager
                    .getInstance(getApplication())
                val operation = workManager
                    .cancelWorkById(notification.notificationId!!)
                operation.result.addListener({
                    Log.d(
                        TAG,
                        "Cancelled the notification worker with uuid: " + notification.notificationId)
                    Log.d(
                        TAG,
                        "For notification with offset " + notification.timeOffsetUnit.toString()
                                + " and amount " + notification.offsetAmount
                    )
                }, Executors.newSingleThreadExecutor())
            }

            // Remove from repository
            deleteNotificationFromRepository(notification)
        }
    }

    fun deleteNotificationFromRepository(notification: ItemNotification) {
        Log.d(
            TAG,
            "Deleting notification from repository. " + notification.offsetAmount
                    + notification.timeOffsetUnit + " " + notification.notificationId
        )
        viewModelScope.launch {
            mItemRepository.deleteNotification(notification)
        }
    }

    /**
     * Schedules all non-scheduled notifications.
     */
    fun scheduleNotifications() {

        // TODO Check for possible race conditions where id for the item might not be set yet
        for (notification in notifications) {
            if (notification.notificationId == null) {
                // Notification needs to be scheduled
                val uuid = scheduleNotification(
                    getApplication(),
                    currentItem,
                    notification
                )
                if (uuid != null) {
                    notification.notificationId = uuid
                    notification.itemId = currentItem.id
                    viewModelScope.launch {
                        mItemRepository.addNotification(notification)
                    }
                    Log.d(TAG, "Scheduled a notification with UUID: $uuid")
                }
            } else {
                // Notification got scheduled before
            }
        }
    }

    fun getItemAndNotifications(itemId: Long): LiveData<ItemAndNotifications> {
        return mItemRepository.getItemAndNotificationsLiveData(itemId)
    }

    /**
     * Saves the current item to the repository and performs all necessary operations for correct notification scheduling,
     * considering all the possible changes due to changing dates and added or removed notifications.
     */
    fun save() {
        Log.d(TAG, "Saving the current item...")
        if (currentItem.condition != Condition.FROZEN) {
            currentItem.frozenAtDate = null
        }

        // Always update the last changed at date
        currentItem.lastChangedAtDate = TimeHelper.currentDateTimeLocalized
        viewModelScope.launch { mItemRepository.insertItem(currentItem) }

        // TODO Check for weird LiveData updates and possible problems with scheduling and deleting
        //  Might need switching these instructions around or possibly more effort
        removeNotificationsToDelete()
        scheduleNotifications()
        Log.d(TAG, "Save completed.")
    }

    /**
     * Creates a new preliminary notification if there is none with the given parameters.
     *
     * @param offsetAmount   The amount that the notification should be offset by.
     * @param offsetTimeUnit The time unit for the notification offset
     * @return Null, if there already is a notification with the given parameters. The new notification, otherwise.
     */
    fun addNotification(offsetAmount: Int, offsetTimeUnit: TimeOffsetUnit): ItemNotification? {

        // Check if there already is a notification with the selected offset and unit
        for ((_, _, _, timeOffsetUnit, offsetAmount1) in notifications) {
            if (offsetAmount1 == offsetAmount && (timeOffsetUnit
                            == offsetTimeUnit)) {
                // TODO Check if the existing notification should be returned instead
                return null
            }
        }

        // TODO Check if -1 is intended as itemId
        val itemNotification = ItemNotification(0, null, -1, offsetTimeUnit, offsetAmount)
        notifications.add(itemNotification)
        return itemNotification
    }

    val currentNotifications: List<ItemNotification>
        get() = notifications

    /**
     * Remember to update the list in the recyclerview after calling this method since the list in the RV is immutable and needs to be updated
     *
     * @param date
     */
    fun updateBestBefore(date: LocalDate?) {

        // Precondition: Date should have changed
        if (currentItem.bestBeforeDate.isEqual(date)) {
            // Noop
            return
        }
        currentItem.bestBeforeDate = date!!
        val newNotifications = ArrayList<ItemNotification>()
        if (editing) {
            for (notification in notifications) {
                val notificationCopy = ItemNotification(
                    0,
                    null,
                    currentItem.id,
                    notification.timeOffsetUnit,
                    notification.offsetAmount
                )

                // TODO Check if the delete thing should be here
                notificationsToDelete.add(notification)
                Log.d(
                    TAG, String.format(
                        "Marking notification %d %s as to delete due to best before date change.",
                        notification.offsetAmount,
                        notification
                            .timeOffsetUnit
                            .toString()
                    ))
                newNotifications.add(notificationCopy)
            }
            notifications = newNotifications
        }
    }

    companion object {
        const val TAG = "AddItemViewModel"
    }

}