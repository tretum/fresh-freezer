package com.mmutert.freshfreezer.util

import androidx.fragment.app.Fragment
import com.mmutert.freshfreezer.ViewModelFactory
import com.mmutert.freshfreezer.data.ItemDatabase
import com.mmutert.freshfreezer.data.ItemRepository

fun Fragment.getViewModelFactory(): ViewModelFactory {
    val repository = ItemRepository(
        ItemDatabase.getDatabase(requireContext().applicationContext).itemDao(),
        ItemDatabase.getDatabase(requireContext().applicationContext).notificationDao()
    )
    return ViewModelFactory(requireActivity().application, repository, this)
}
