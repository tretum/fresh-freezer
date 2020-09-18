package com.mmutert.freshfreezer.ui.itemlist

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import androidx.work.WorkManager
import com.mmutert.freshfreezer.data.Condition
import com.mmutert.freshfreezer.data.ItemNotification
import com.mmutert.freshfreezer.data.ItemRepository
import com.mmutert.freshfreezer.data.StorageItem
import com.mmutert.freshfreezer.ui.itemlist.SortingOption.DATE_BEST_BEFORE
import com.mmutert.freshfreezer.ui.itemlist.SortingOption.SortingOrder
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import kotlin.math.max

class ItemListViewModel(
        private val application: Application,
        private val savedStateHandle: SavedStateHandle,
        private val repository: ItemRepository) : ViewModel() {

    var storageItems: LiveData<List<StorageItem>> = repository.allActiveStorageItems
        private set

    var sortingOrder = loadSortingOrderPreference()
        set(value) {
            field = value
            storeSortingOrderPreference()
        }
    var sortingOption = loadSortingOptionPreference()
        set(value) {
            field = value
            storeSortingOptionPreference()
        }

    fun filterItems(conditions: Collection<Condition?>) {
        storageItems = Transformations.map(storageItems) { input: List<StorageItem> ->
            input.filter { conditions.contains(it.condition) }
        }
    }

    fun resetFilter() {
        storageItems = repository.allActiveStorageItems
    }

    private fun loadSortingOrderPreference(): SortingOrder {
        val preferences = PreferenceManager.getDefaultSharedPreferences(application)
        val orderString = preferences.getString(SORTING_ORDER_KEY, "ASCENDING")
        return SortingOrder.valueOf(orderString!!)
    }

    private fun storeSortingOrderPreference() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(application)
        preferences.edit().putString(SORTING_ORDER_KEY, sortingOrder.toString()).apply()
    }

    private fun loadSortingOptionPreference(): SortingOption {
        val preferences = PreferenceManager.getDefaultSharedPreferences(application)
        val orderString = preferences.getString(SORTING_OPTION_KEY, "DATE_BEST_BEFORE")

        return SortingOption.valueOf(orderString!!)
    }

    private fun storeSortingOptionPreference() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(application)
        preferences.edit().putString(SORTING_OPTION_KEY, sortingOption.toString()).apply()
    }

    fun updateItem(item: StorageItem) {
        viewModelScope.launch {
            repository.updateItem(item)
        }
    }

    fun delete(item: StorageItem) {
        viewModelScope.launch {
            repository.deleteItem(item)
        }
    }

    fun archive(item: StorageItem) {
        viewModelScope.launch {
            repository.archiveItem(item)
        }
    }

    fun restore(item: StorageItem) {
        viewModelScope.launch {
            repository.restoreItem(item)
        }
    }


    /*
     * Notifications
     */
    /**
     * Get all scheduled notifications for the given item
     * @param item The item to retrieve the scheduled notifications for.
     * @return The list of notifications for the item.
     */
    fun getAllNotifications(item: StorageItem): List<ItemNotification> {
        return repository.getAllNotifications(item)
    }

    /**
     * Cancels all notification workers for the given item.
     * @param itemToArchive The item for which the notification workers should be cancelled.
     */
    fun cancelNotifications(itemToArchive: StorageItem) {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            val allNotifications = getAllNotifications(itemToArchive)

            // Cancel all notifications
            val workManager = WorkManager.getInstance(application)
            for ((_, notificationId) in allNotifications) {
                workManager.cancelWorkById(notificationId!!)
                Log.d(TAG, "Cancelled the notification worker with uuid: $notificationId")
            }
        }
    }

    /**
     * Take the given amount from the given model and update the item in the repository
     * @param item The item to take from
     * @param amountTaken The amount that was taken from the item
     */
    fun takeFromItem(item: StorageItem, amountTaken: Float) {
        val newAmount = max(0.0f, item.amount - amountTaken)
        // TODO Check for possible side effects and possibly create copy of item first
        item.amount = newAmount
        updateItem(item)
    }

    companion object {
        const val TAG = "ItemListViewModel"
        private const val SORTING_ORDER_KEY = "sortingOrder"
        private const val SORTING_OPTION_KEY = "sortingOption"
    }
}