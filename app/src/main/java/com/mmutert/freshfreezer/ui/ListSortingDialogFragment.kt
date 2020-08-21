package com.mmutert.freshfreezer.ui

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.CompoundButton
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mmutert.freshfreezer.R
import com.mmutert.freshfreezer.databinding.DialogFilterActionOptionsBinding
import com.mmutert.freshfreezer.util.SortingOption
import com.mmutert.freshfreezer.util.SortingOption.SortingOrder

class ListSortingDialogFragment(
        private var currentOption: SortingOption,
        private var currentOrder: SortingOrder,
        private val listener: ListSortingChangedListener) : DialogFragment() {

    interface ListSortingChangedListener {
        fun listOptionClicked(selectedSortingOption: SortingOption, sortingOrder: SortingOrder)
    }

    private lateinit var mBinding: DialogFilterActionOptionsBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        mBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.dialog_filter_action_options,
            null,
            false
        )
        when (currentOrder) {
            SortingOrder.ASCENDING -> mBinding.rbSortOrderAscending.isChecked = true
            SortingOrder.DESCENDING -> mBinding.rbSortOrderDescending.isChecked = true
        }
        when (currentOption) {
            SortingOption.DATE_ADDED -> mBinding.rbSortOptionAddedDate.isChecked = true
            SortingOption.DATE_CHANGED -> mBinding.rbSortOptionLastChangedDate.isChecked = true
            SortingOption.DATE_FROZEN_AT -> mBinding.rbSortOptionFrozenAt.isChecked = true
            SortingOption.DATE_BEST_BEFORE -> mBinding.rbSortOptionBestBefore.isChecked = true
            SortingOption.NAME -> mBinding.rbSortOptionName.isChecked = true
        }
        mBinding.rbSortOrderAscending.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            if (isChecked) {
                currentOrder = SortingOrder.ASCENDING
            }
        }
        mBinding.rbSortOrderDescending.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            if (isChecked) {
                currentOrder = SortingOrder.DESCENDING
            }
        }
        mBinding.rbSortOptionName.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            if (isChecked) {
                currentOption = SortingOption.NAME
            }
        }
        mBinding.rbSortOptionAddedDate.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            if (isChecked) {
                currentOption = SortingOption.DATE_ADDED
            }
        }
        mBinding.rbSortOptionLastChangedDate.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            if (isChecked) {
                currentOption = SortingOption.DATE_CHANGED
            }
        }
        mBinding.rbSortOptionBestBefore.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            if (isChecked) {
                currentOption = SortingOption.DATE_BEST_BEFORE
            }
        }
        mBinding.rbSortOptionFrozenAt.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            if (isChecked) {
                currentOption = SortingOption.DATE_FROZEN_AT
            }
        }
        return MaterialAlertDialogBuilder(requireContext())
            .setView(mBinding.root)
            .setPositiveButton(R.string.dialog_sorting_options_select_button_label) { dialog: DialogInterface?, which: Int ->
                listener.listOptionClicked(
                    currentOption,
                    currentOrder)
            }
            .setNegativeButton(R.string.dialog_sorting_options_close_button_label) { dialog: DialogInterface?, which: Int -> }
            .create()
    }
}