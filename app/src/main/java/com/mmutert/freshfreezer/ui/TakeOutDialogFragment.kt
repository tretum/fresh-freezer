package com.mmutert.freshfreezer.ui

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mmutert.freshfreezer.R
import com.mmutert.freshfreezer.data.AmountUnit.Companion.getFormatterForUnit
import com.mmutert.freshfreezer.data.FrozenItem
import com.mmutert.freshfreezer.databinding.DialogTakeItemBinding

class TakeOutDialogFragment(private val listener: TakeOutDialogClickListener,
                            val item: FrozenItem) : DialogFragment() {
    private lateinit var binding: DialogTakeItemBinding

    interface TakeOutDialogClickListener {
        fun onPositiveClicked(dialog: TakeOutDialogFragment)
        fun onTakeAllClicked(dialog: TakeOutDialogFragment)
        fun onCancelClicked(dialog: TakeOutDialogFragment)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DataBindingUtil.inflate(
            layoutInflater, R.layout.dialog_take_item, null, false)
        binding.item = item
        val unitString = requireContext().resources.getString(item.unit.stringResId)
        binding.takeDialogUnit.text = unitString
        val numberInstance = getFormatterForUnit(item.unit)
        val amountAsString = numberInstance.format(item.amount.toDouble())
        binding.takeDialogCurrentAmount.text = "$amountAsString $unitString"

        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setPositiveButton(R.string.dialog_take_accept_button_label) { dialog: DialogInterface?, which: Int ->
                listener.onPositiveClicked(
                    this)
            }
            .setNeutralButton(R.string.dialog_take_button_take_all_label) { dialog: DialogInterface?, which: Int ->
                listener.onTakeAllClicked(
                    this)
            }
            .setNegativeButton(R.string.dialog_take_button_cancel_label) { dialog: DialogInterface?, which: Int ->
                listener.onCancelClicked(
                    this)
            }.create()
    }

    val selectionAmount: Float
        get() = try {
            binding.etTakeDialogSelectedAmount.text.toString().toFloat()
        } catch (e: NumberFormatException) {
            0f
        }
}