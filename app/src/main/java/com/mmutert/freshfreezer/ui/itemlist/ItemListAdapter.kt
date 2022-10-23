package com.mmutert.freshfreezer.ui.itemlist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mmutert.freshfreezer.data.AmountUnit.Companion.getFormatterForUnit
import com.mmutert.freshfreezer.data.StorageItem
import com.mmutert.freshfreezer.databinding.ItemOverviewItemBinding
import com.mmutert.freshfreezer.ui.dialogs.ListSortingDialogFragment.ListSortingChangedListener
import com.mmutert.freshfreezer.ui.itemlist.ItemListAdapter.ItemListAdapterViewHolder
import com.mmutert.freshfreezer.ui.itemlist.SortingOption.SortingOrder
import org.joda.time.format.DateTimeFormat
import java.util.*

class ItemListAdapter(
    private val mViewModel: ItemListViewModel,
    private val itemClickedCallback: ListItemClickedCallback,
    private val context: Context
) :
    RecyclerView.Adapter<ItemListAdapterViewHolder>(), ListSortingChangedListener {

    private val mDiffer = AsyncListDiffer(this, DiffCallBack())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemListAdapterViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemOverviewItemBinding.inflate(inflater, parent, false)
        return ItemListAdapterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemListAdapterViewHolder, position: Int) {
        val binding = holder.binding
        val itemAtPosition = getItemAtPosition(position)

        val formatter = DateTimeFormat.longDate().withLocale(Locale.getDefault())
        val numberInstance = getFormatterForUnit(itemAtPosition.unit)

        binding.apply {
            item = itemAtPosition

            tvAmount.text = numberInstance.format(itemAtPosition.amount.toDouble())
            tvBestBeforeDate.text = formatter.print(itemAtPosition.bestBeforeDate)
            if (itemAtPosition.frozenAtDate != null) {
                tvDateFrozen.text = formatter.print(itemAtPosition.frozenAtDate)
            }
            root.setOnClickListener { itemClickedCallback.onClick(itemAtPosition) }
            listItemDeleteBackground.visibility = View.INVISIBLE
            listItemTakeBackground.visibility = View.INVISIBLE
        }
    }

    override fun getItemCount(): Int {
        return mDiffer.currentList.size
    }

    fun getItemAtPosition(position: Int): StorageItem {
        return mDiffer.currentList[position]
    }

    /**
     * Returns the current position of the given item as the index of the item in the list for the adapter
     * @param item The item to get the position for
     * @return The index of the item.
     */
    fun getPositionOfItem(item: StorageItem): Int {
        return mDiffer.currentList.indexOf(item)
    }

    override fun listOptionClicked(
        sortingOption: SortingOption,
        sortingOrder: SortingOrder
    ) {
        mViewModel.sortingOption = sortingOption
        mViewModel.sortingOrder = sortingOrder
        itemList = mDiffer.currentList
    }

    /**
     * Sorts the given list of items according to the sorting option and sorting order that are currently in the view model.
     * Sorting is done in-place.
     *
     * @param items  The list of items to sort.
     */
    private fun sortItems(items: MutableList<StorageItem>) {
        if (items.isNotEmpty()) {
            when (mViewModel.sortingOption) {
                SortingOption.DATE_CHANGED -> items.sortWith(Comparator { (_, _, _, _, _, _, _, lastChangedAtDate1), (_, _, _, _, _, _, _, lastChangedAtDate2) ->
                    val result = lastChangedAtDate1.compareTo(lastChangedAtDate2)
                    if (mViewModel.sortingOrder == SortingOrder.ASCENDING) {
                        return@Comparator result
                    } else {
                        return@Comparator result * -1
                    }
                })
                SortingOption.DATE_ADDED -> items.sortWith(Comparator { (_, _, _, _, _, _, itemCreationDate1), (_, _, _, _, _, _, itemCreationDate2) ->
                    val result = itemCreationDate1.compareTo(itemCreationDate2)
                    if (mViewModel.sortingOrder == SortingOrder.ASCENDING) {
                        return@Comparator result
                    } else {
                        return@Comparator result * -1
                    }
                })
                SortingOption.DATE_FROZEN_AT -> items.sortWith(Comparator { (_, _, _, _, frozenAtDate1), (_, _, _, _, frozenAtDate2) ->
                    var result = 0
                    if (frozenAtDate1 != null && frozenAtDate2 != null) {
                        result = frozenAtDate1.compareTo(frozenAtDate2)
                    } else if (frozenAtDate1 == null && frozenAtDate2 != null) {
                        result = -1
                    } else if (frozenAtDate1 != null && frozenAtDate2 == null) {
                        result = 1
                    }
                    if (mViewModel.sortingOrder == SortingOrder.ASCENDING) {
                        return@Comparator result
                    } else {
                        return@Comparator result * -1
                    }
                })
                SortingOption.DATE_BEST_BEFORE -> items.sortWith(Comparator { (_, _, _, _, _, bestBeforeDate1), (_, _, _, _, _, bestBeforeDate2) ->
                    val result = bestBeforeDate1.compareTo(bestBeforeDate2)
                    if (mViewModel.sortingOrder == SortingOrder.ASCENDING) {
                        return@Comparator result
                    } else {
                        return@Comparator result * -1
                    }
                })
                SortingOption.NAME -> items.sortWith(Comparator { (_, name), (_, name2) ->
                    val result =
                        name.lowercase(Locale.ROOT).compareTo(name2.lowercase(Locale.ROOT))
                    if (mViewModel.sortingOrder == SortingOrder.ASCENDING) {
                        return@Comparator result
                    } else {
                        return@Comparator result * -1
                    }
                })
            }
        }
    }

    var itemList: List<StorageItem>
        get() = mDiffer.currentList
        set(value) {
            val newItems = value.toMutableList()
            sortItems(newItems)
            mDiffer.submitList(newItems)
        }

    /**
     * The view holder
     */
    class ItemListAdapterViewHolder(val binding: ItemOverviewItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    class DiffCallBack : DiffUtil.ItemCallback<StorageItem>() {
        override fun areItemsTheSame(
            oldStorageItem: StorageItem, newStorageItem: StorageItem
        ): Boolean {
            // FrozenItem properties may have changed if reloaded from the DB, but ID is fixed
            return oldStorageItem.id == newStorageItem.id
        }

        override fun areContentsTheSame(
            oldStorageItem: StorageItem, newStorageItem: StorageItem
        ): Boolean {
            // NOTE: if you use equals, your object must properly override Object#equals()
            // Incorrectly returning false here will result in too many animations.
            return oldStorageItem == newStorageItem
        }
    }

    companion object {
        val LOG_TAG = ItemListAdapter::class.simpleName
    }
}