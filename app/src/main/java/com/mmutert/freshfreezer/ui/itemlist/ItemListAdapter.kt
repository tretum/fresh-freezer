package com.mmutert.freshfreezer.ui.itemlist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mmutert.freshfreezer.data.AmountUnit.Companion.getFormatterForUnit
import com.mmutert.freshfreezer.data.FrozenItem
import com.mmutert.freshfreezer.databinding.ListItemBinding
import com.mmutert.freshfreezer.ui.ListSortingDialogFragment.ListSortingChangedListener
import com.mmutert.freshfreezer.ui.itemlist.ItemListAdapter.ItemListAdapterViewHolder
import com.mmutert.freshfreezer.util.SortingOption
import com.mmutert.freshfreezer.util.SortingOption.SortingOrder
import com.mmutert.freshfreezer.viewmodel.ItemListViewModel
import org.joda.time.format.DateTimeFormat
import java.util.*

class ItemListAdapter(
        private val mViewModel: ItemListViewModel,
        private val itemClickedCallback: ListItemClickedCallback,
        private val context: Context) :
        RecyclerView.Adapter<ItemListAdapterViewHolder>(), ListSortingChangedListener {

    private val mDiffer = AsyncListDiffer(this, DIFF_CALLBACK)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemListAdapterViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemBinding.inflate(inflater, parent, false)
        return ItemListAdapterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemListAdapterViewHolder, position: Int) {
        val binding = holder.binding
        val itemForPosition = getItemAtPosition(position)
        binding.item = itemForPosition

        // Set name of the item
        if (itemForPosition.name.isNotEmpty()) {
            binding.tvItemName.text = itemForPosition.name
        }
        val unit = itemForPosition.unit
        binding.tvAmountUnit.text = context.resources.getString(unit.stringResId)
        val numberInstance = getFormatterForUnit(itemForPosition.unit)
        val amountText = numberInstance.format(itemForPosition.amount.toDouble())
        binding.tvAmount.text = amountText
        val formatter = DateTimeFormat.longDate().withLocale(Locale.getDefault())
        val bestBeforeDate = itemForPosition.bestBeforeDate
        val bestBeforeFormatted = formatter.print(bestBeforeDate)
        binding.tvBestBeforeDate.text = bestBeforeFormatted
        val frozenDate = itemForPosition.frozenAtDate
        if (frozenDate != null) {
            binding.tvDateFrozen.visibility = View.VISIBLE
            binding.tvFrozenDateTitle.visibility = View.VISIBLE
            val frozenFormatted = formatter.print(frozenDate)
            binding.tvDateFrozen.text = frozenFormatted
        } else {
            binding.tvFrozenDateTitle.visibility = View.GONE
            binding.tvDateFrozen.visibility = View.GONE
        }
        binding.root.setOnClickListener { itemClickedCallback.onClick(itemForPosition) }
        binding.listItemDeleteBackground.visibility = View.INVISIBLE
        binding.listItemTakeBackground.visibility = View.INVISIBLE
    }

    override fun getItemCount(): Int {
        return mDiffer.currentList.size
    }

    fun getItemAtPosition(position: Int): FrozenItem {
        return mDiffer.currentList[position]
    }

    /**
     * Returns the current position of the given item as the index of the item in the list for the adapter
     * @param item The item to get the position for
     * @return The index of the item.
     */
    fun getPositionOfItem(item: FrozenItem): Int {
        return mDiffer.currentList.indexOf(item)
    }

    override fun listOptionClicked(selectedSortingOption: SortingOption,
                                   sortingOrder: SortingOrder) {
        mViewModel.sortingOption = selectedSortingOption
        mViewModel.sortingOrder = sortingOrder
        itemList = mDiffer.currentList
    }

    /**
     * Sorts the given list of items according to the sorting option and sorting order that are currently in the view model.
     * Sorting is done in-place.
     *
     * @param items  The list of items to sort.
     */
    private fun sortItems(items: MutableList<FrozenItem>) {
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
                            name.toLowerCase(Locale.ROOT).compareTo(name2.toLowerCase(Locale.ROOT))
                    if (mViewModel.sortingOrder == SortingOrder.ASCENDING) {
                        return@Comparator result
                    } else {
                        return@Comparator result * -1
                    }
                })
            }
        }
    }

    var itemList: List<FrozenItem>
        get() = mDiffer.currentList
        set(value) {
            val newItems = value.toMutableList()
            sortItems(newItems)
            mDiffer.submitList(newItems)
        }

    /**
     * The view holder
     */
    class ItemListAdapterViewHolder(val binding: ListItemBinding) :
            RecyclerView.ViewHolder(binding.root)

    companion object {
        /**
         * The Callback for the DiffUtil.
         */
        private val DIFF_CALLBACK: DiffUtil.ItemCallback<FrozenItem> =
                object : DiffUtil.ItemCallback<FrozenItem>() {
                    override fun areItemsTheSame(
                            oldFrozenItem: FrozenItem, newFrozenItem: FrozenItem): Boolean {
                        // FrozenItem properties may have changed if reloaded from the DB, but ID is fixed
                        return oldFrozenItem.id == newFrozenItem.id
                    }

                    override fun areContentsTheSame(
                            oldFrozenItem: FrozenItem, newFrozenItem: FrozenItem): Boolean {
                        // NOTE: if you use equals, your object must properly override Object#equals()
                        // Incorrectly returning false here will result in too many animations.
                        return oldFrozenItem == newFrozenItem
                    }
                }
    }
}