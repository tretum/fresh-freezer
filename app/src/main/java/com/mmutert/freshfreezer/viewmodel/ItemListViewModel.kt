package com.mmutert.freshfreezer.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import androidx.work.WorkManager
import com.mmutert.freshfreezer.data.Condition
import com.mmutert.freshfreezer.data.FrozenItem
import com.mmutert.freshfreezer.data.ItemDatabase.Companion.getDatabase
import com.mmutert.freshfreezer.data.ItemNotification
import com.mmutert.freshfreezer.data.ItemRepository
import com.mmutert.freshfreezer.util.SortingOption
import com.mmutert.freshfreezer.util.SortingOption.SortingOrder
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import kotlin.math.max

class ItemListViewModel(application: Application) : AndroidViewModel(application) {

    private val mItemRepository: ItemRepository = ItemRepository(getDatabase(application).itemDao())

    var frozenItems: LiveData<List<FrozenItem>> = mItemRepository.allActiveFrozenItems
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
        frozenItems = Transformations.map(frozenItems) { input: List<FrozenItem> ->
            input.filter { conditions.contains(it.condition) }
        }
    }

    fun resetFilter() {
        frozenItems = mItemRepository.allActiveFrozenItems
    }

    private fun loadSortingOrderPreference(): SortingOrder {
        val preferences = PreferenceManager.getDefaultSharedPreferences(getApplication())
        val orderString = preferences.getString(SORTING_ORDER_KEY, "ASCENDING")
        return SortingOrder.valueOf(orderString!!)
    }

    private fun storeSortingOrderPreference() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(getApplication())
        preferences.edit().putString(SORTING_ORDER_KEY, sortingOrder.toString()).apply()
    }

    private fun loadSortingOptionPreference(): SortingOption {
        val preferences = PreferenceManager.getDefaultSharedPreferences(getApplication())
        val orderString = preferences.getString(SORTING_OPTION_KEY, "DATE_BEST_BEFORE")

        return SortingOption.valueOf(orderString!!)
    }

    private fun storeSortingOptionPreference() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(getApplication())
        preferences.edit().putString(SORTING_OPTION_KEY, sortingOption.toString()).apply()
    }

    fun updateItem(item: FrozenItem) {
        viewModelScope.launch {
            mItemRepository.updateItem(item)
        }
    }

    fun delete(item: FrozenItem) {
        viewModelScope.launch {
            mItemRepository.deleteItem(item)
        }
    }

    fun archive(item: FrozenItem) {
        viewModelScope.launch {
            mItemRepository.archiveItem(item)
        }
    }

    fun restore(item: FrozenItem) {
        viewModelScope.launch {
            mItemRepository.restoreItem(item)
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
    fun getAllNotifications(item: FrozenItem): List<ItemNotification> {
        return mItemRepository.getAllNotifications(item)
    }

    /**
     * Cancels all notification workers for the given item.
     * @param itemToArchive The item for which the notification workers should be cancelled.
     */
    fun cancelNotifications(itemToArchive: FrozenItem) {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            val allNotifications = getAllNotifications(itemToArchive)

            // Cancel all notifications
            val workManager = WorkManager.getInstance(getApplication())
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
    fun takeFromItem(item: FrozenItem, amountTaken: Float) {
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