package com.mmutert.freshfreezer

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.mmutert.freshfreezer.data.ItemRepository
import com.mmutert.freshfreezer.ui.additem.AddItemViewModel
import com.mmutert.freshfreezer.ui.itemlist.ItemListViewModel

/**
 * Factory for all ViewModels.
 */
@Suppress("UNCHECKED_CAST")
class ViewModelFactory constructor(
        private val application: Application,
        private val repository: ItemRepository,
        owner: SavedStateRegistryOwner,
        defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {

    override fun <T : ViewModel> create(
            key: String,
            modelClass: Class<T>,
            handle: SavedStateHandle
    ) = with(modelClass) {
        when {
            isAssignableFrom(ItemListViewModel::class.java)     ->
                ItemListViewModel(application, handle, repository)
            isAssignableFrom(AddItemViewModel::class.java) ->
                AddItemViewModel(application, handle, repository)
            else                                                ->
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    } as T
}
