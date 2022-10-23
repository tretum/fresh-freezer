package com.mmutert.freshfreezer.ui.additem

import android.app.Application
import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.*
import androidx.work.WorkManager
import com.mmutert.freshfreezer.Event
import com.mmutert.freshfreezer.R
import com.mmutert.freshfreezer.data.*
import com.mmutert.freshfreezer.notification.NotificationHelper.scheduleNotification
import com.mmutert.freshfreezer.util.TimeHelper
import kotlinx.coroutines.launch
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.Executors

class AddItemViewModel(
    private val application: Application,
    private val savedStateHandle: SavedStateHandle,
    private val repository: ItemRepository
) : ViewModel() {

    private var isNewItem: Boolean = true

    private val DATE_FORMATTER: DateTimeFormatter =
        DateTimeFormat.longDate().withLocale(Locale.getDefault())

    private var _notifications: MutableLiveData<List<ItemNotification>> =
        MutableLiveData(ArrayList())
    val notifications: LiveData<List<ItemNotification>> = _notifications

    private var notificationsToDelete: MutableList<ItemNotification> = ArrayList()

    private var editing = false

    private var itemId: Long = 0
    private var itemCreationDate: LocalDateTime? = null

    val itemName = MutableLiveData("")
    val itemNotes = MutableLiveData("")

    val storedAmountString = MutableLiveData("0")

    private val _selectedCondition = MutableLiveData(Condition.CHILLED)
    val selectedCondition: LiveData<Condition> = _selectedCondition

    private val _frozenDate: MutableLiveData<LocalDate> = MutableLiveData()
    val frozenDateFormatted = Transformations.map(_frozenDate) {
        it?.let {
            DATE_FORMATTER.print(it)
        } ?: ""
    }
    val frozenDate: LiveData<LocalDate> = _frozenDate

    private val _bestBeforeDate = MutableLiveData(TimeHelper.currentDateLocalized)
    val bestBeforeDateFormatted = Transformations.map(_bestBeforeDate) {
        DATE_FORMATTER.print(it)
    }
    val bestBeforeDate: LiveData<LocalDate> = _bestBeforeDate

    private val _selectedUnit = MutableLiveData<AmountUnit>()
    val selectedUnit: LiveData<AmountUnit> = _selectedUnit

    // EVENTS
    private val _addNotificationEvent = MutableLiveData<Event<Unit>>()
    val addNotificationEvent: LiveData<Event<Unit>> = _addNotificationEvent

    private val _personSaved = MutableLiveData<Event<Unit>>()
    val personSaved: LiveData<Event<Unit>> = _personSaved

    private val _snackbarTextId = MutableLiveData<Event<Int>>()
    val snackbarTextId: LiveData<Event<Int>> = _snackbarTextId

    private val _bestBeforeButtonEvent = MutableLiveData<Event<Unit>>()
    val bestBeforeButtonEvent: LiveData<Event<Unit>> = _bestBeforeButtonEvent

    private val _frozenDateButtonEvent = MutableLiveData<Event<Unit>>()
    val frozenDateButtonEvent: LiveData<Event<Unit>> = _frozenDateButtonEvent

    // FUNCTIONS
    fun setNotifications(notifications: List<ItemNotification>) {
        _notifications.value = notifications
    }


    fun start(itemId: Long = 0L) {
        if (itemId > 0L) {
            this.isNewItem = false

            // Load the transaction
            viewModelScope.launch {
                val itemAndNotifications = repository.getItemAndNotifications(itemId)
                onItemLoaded(itemAndNotifications.item)
                setNotifications(itemAndNotifications.notifications)
            }
        }
    }

    private fun onItemLoaded(item: StorageItem) {
        itemNotes.value = item.notes
        itemName.value = item.name
        storedAmountString.value = item.amount.toString()
        _bestBeforeDate.value = item.bestBeforeDate
        _frozenDate.value = item.frozenAtDate
        _selectedUnit.value = item.unit
        _selectedCondition.value = item.condition
        itemId = item.id
        itemCreationDate = item.itemCreationDate
    }

    /**
     * Mark a notification as being deleted on the UI and to be permanently removed on saving the item.
     *
     * @param notification The notification to mark as deleted.
     */
    fun addNotificationToDelete(notification: ItemNotification) {
        Log.d(
            TAG,
            "Adding notification ${notification.offsetAmount}${notification.timeOffsetUnit} to the delete list."
        )
        notificationsToDelete.add(notification)
        // TODO Missing null check, but should work
        val oldList = _notifications.value!!.toMutableList()
        oldList.remove(notification)
        _notifications.value = oldList
    }

    /**
     * Cancels notifications pending deletion and removes them from the repository
     */
    private fun removeNotificationsToDelete() {
        for (notification in notificationsToDelete) {
            // Remove notification workers
            if (notification.notificationId != null) {
                val workManager = WorkManager
                    .getInstance(application)
                val operation = workManager
                    .cancelWorkById(notification.notificationId!!)
                operation.result.addListener({
                    Log.d(
                        TAG,
                        "Cancelled the notification worker with uuid: " + notification.notificationId
                    )
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

    private fun deleteNotificationFromRepository(notification: ItemNotification) {
        Log.d(
            TAG,
            "Deleting notification from repository. " + notification.offsetAmount
                    + notification.timeOffsetUnit + " " + notification.notificationId
        )
        viewModelScope.launch {
            repository.deleteNotification(notification)
        }
    }

    /**
     * Schedules all non-scheduled notifications.
     */
    private fun scheduleNotifications(item: StorageItem) {

        if (_notifications.value == null) {
            return
        }

        // TODO Check for possible race conditions where id for the item might not be set yet
        for (notification in _notifications.value!!) {
            if (notification.notificationId == null) {
                // Notification needs to be scheduled
                val uuid = scheduleNotification(
                    application,
                    item,
                    notification
                )
                if (uuid != null) {
                    notification.notificationId = uuid
                    notification.itemId = itemId
                    viewModelScope.launch {
                        repository.addNotification(notification)
                    }
                    Log.d(TAG, "Scheduled a notification with UUID: $uuid")
                }
            } else {
                // Notification got scheduled before
            }
        }
    }

    /**
     * Saves the current item to the repository and performs all necessary operations for correct notification scheduling,
     * considering all the possible changes due to changing dates and added or removed notifications.
     */
    fun save() {
        Log.d(TAG, "Saving the current item...")

        if (_bestBeforeDate.value == null) {
            return
        }

        // Input Check: The best before date should not be after the freezing date, if that is specified
        if (_frozenDate.value != null && _frozenDate.value!!.isAfter(_bestBeforeDate.value)) {
            showSnackbarMessage(R.string.add_item_bbd_before_freezing_date_error)
            return
        }
        if (_bestBeforeDate.value!!.isBefore(TimeHelper.currentDateLocalized)) {
            showSnackbarMessage(R.string.add_item_bbd_before_current_date_error)
            return
        }

        // Always update the last changed at date
        val amount: Float = try {
            storedAmountString.value?.toFloat() ?: 0f
        } catch (e: Exception) {
            0f
        }

        val frozenDateToStore = if (_selectedCondition.value == Condition.FROZEN) {
            _frozenDate.value
        } else {
            null
        }

        val item = StorageItem(
            itemId,
            itemName.value ?: "",
            amount,
            _selectedUnit.value ?: AmountUnit.GRAMS,
            frozenDateToStore,
            _bestBeforeDate.value ?: TimeHelper.currentDateLocalized.plusDays(1),
            itemCreationDate ?: TimeHelper.currentDateTimeLocalized,
            TimeHelper.currentDateTimeLocalized,
            itemNotes.value,
            _selectedCondition.value ?: Condition.CHILLED,
            false
        )

        viewModelScope.launch {
            repository.insertItem(item)
        }

        // TODO Check for weird LiveData updates and possible problems with scheduling and deleting
        //  Might need switching these instructions around or possibly more effort
        removeNotificationsToDelete()
        scheduleNotifications(item)
        Log.d(TAG, "Save completed.")

        _personSaved.value = Event(Unit)
    }

    /**
     * Creates a new preliminary notification if there is none with the given parameters.
     *
     * @param offsetAmount   The amount that the notification should be offset by.
     * @param offsetTimeUnit The time unit for the notification offset
     * @return Null, if there already is a notification with the given parameters. The new notification, otherwise.
     */
    fun addNotification(offsetAmount: Int, offsetTimeUnit: TimeOffsetUnit) {

        val isNew = _notifications.value?.none {
            it.offsetAmount == offsetAmount && it.timeOffsetUnit == offsetTimeUnit
        }

        if (isNew != null && isNew) {
            // TODO Check if -1 is intended as itemId
            val newNotification = ItemNotification(0, null, -1, offsetTimeUnit, offsetAmount)
            val oldList = _notifications.value!!.toMutableList()
            oldList.add(newNotification)
            _notifications.value = oldList
        }
    }


    /**
     * Remember to update the list in the notifications recyclerview after calling this method,
     *  since the list in the RV is immutable and needs to be updated.
     *
     * @param date The new date to set as best before date of the item
     */
    fun setBestBefore(date: LocalDate) {

        // PART 1: Update the actual date value if it has changed
        if (_bestBeforeDate.value == null || _bestBeforeDate.value != date) {
            _bestBeforeDate.value = date
        } else {
            return
        }

        // PART 2: Update the notifications, if the date has changed

        if (editing) {
            if (_notifications.value == null) {
                return
            }

            val newNotifications = ArrayList<ItemNotification>()

            for (notification in _notifications.value!!) {
                val notificationCopy = notification.copy(id = 0, notificationId = null)

                // TODO Check if the delete thing should be here
                notificationsToDelete.add(notification)
                Log.d(
                    TAG,
                    "Marking notification ${notification.offsetAmount} ${notification.timeOffsetUnit} as to delete due to best before date change.",
                )
                newNotifications.add(notificationCopy)
            }
            setNotifications(newNotifications)
        }
    }

    fun addNotificationClicked() {
        _addNotificationEvent.value = Event(Unit)
    }

    fun bestBeforeButtonClicked() {
        _bestBeforeButtonEvent.value = Event(Unit)
    }

    fun frozenDateButtonClicked() {
        _frozenDateButtonEvent.value = Event(Unit)
    }

    fun showSnackbarMessage(@StringRes message: Int) {
        _snackbarTextId.value = Event(message)
    }


    fun setCondition(condition: Condition) {
        if (_selectedCondition.value == null || _selectedCondition.value != condition) {
            _selectedCondition.value = condition
        }
    }

    fun setFrozenDate(date: LocalDate = TimeHelper.currentDateLocalized) {
        _frozenDate.value = date
    }

    fun setAmountUnit(selectedUnit: AmountUnit) {
        if (_selectedUnit.value == null || _selectedUnit.value != selectedUnit) {
            _selectedUnit.value = selectedUnit
        }
    }

    companion object {
        const val TAG = "AddItemViewModel"
    }

}