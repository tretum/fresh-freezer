package com.mmutert.freshfreezer.ui

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.CompoundButton
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mmutert.freshfreezer.R
import com.mmutert.freshfreezer.data.TimeOffsetUnit
import com.mmutert.freshfreezer.databinding.DialogAddNotificationBinding
import com.mmutert.freshfreezer.ui.databinding.IntStringConverter

class NotificationOffsetDialogFragment(private val listener: NotificationOffsetDialogClickListener) :
        DialogFragment() {
    var offSetAmount = TimeOffsetUnit.DAYS
        private set
    private lateinit var dialogBinding: DialogAddNotificationBinding
    var enteredOffset = 1
        private set

    interface NotificationOffsetDialogClickListener {
        fun onPositiveClick(dialog: NotificationOffsetDialogFragment?)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialogBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.dialog_add_notification,
            null,
            false
        )
        setupRadioButtons()

        // Initialize the selected offset after setting up the radio buttons to correctly set the text for the labels
        dialogBinding.selectedOffset = 1
        return MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .setPositiveButton("Done") { dialog: DialogInterface?, which: Int ->
                listener.onPositiveClick(
                    this)
            }
            .create()
    }

    private fun setupRadioButtons() {
        val daysButtonListener =
                CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
                    if (isChecked) {
                        offSetAmount = TimeOffsetUnit.DAYS
                        val quantityString = resources.getQuantityString(
                            R.plurals.days_selected_capitalized,
                            enteredOffset
                        )
                        buttonView.text = quantityString
                    } else {
                        val quantityString = resources.getQuantityString(
                            R.plurals.days_capitalized,
                            enteredOffset
                        )
                        buttonView.text = quantityString
                    }
                }
        val weeksButtonListener =
                CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
                    if (isChecked) {
                        offSetAmount = TimeOffsetUnit.WEEKS
                        val quantityString = resources.getQuantityString(
                            R.plurals.weeks_selected_capitalized,
                            enteredOffset
                        )
                        buttonView.text = quantityString
                    } else {
                        val quantityString = resources.getQuantityString(
                            R.plurals.weeks_capitalized,
                            enteredOffset
                        )
                        buttonView.text = quantityString
                    }
                }
        val monthsButtonListener =
                CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
                    if (isChecked) {
                        offSetAmount = TimeOffsetUnit.MONTHS
                        val quantityString = resources.getQuantityString(
                            R.plurals.months_selected_capitalized,
                            enteredOffset
                        )
                        buttonView.text = quantityString
                    } else {
                        val quantityString = resources.getQuantityString(
                            R.plurals.months_capitalized,
                            enteredOffset
                        )
                        buttonView.text = quantityString
                    }
                }
        dialogBinding.etAddNotificationOffsetAmount.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence,
                                               start: Int,
                                               count: Int,
                                               after: Int) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable) {
                    val value = IntStringConverter.stringToInt(s.toString())
                    if (enteredOffset != value) {
                        enteredOffset = value
                    }
                    daysButtonListener.onCheckedChanged(
                        dialogBinding.radioButtonDays,
                        dialogBinding.radioButtonDays.isChecked)
                    weeksButtonListener.onCheckedChanged(
                        dialogBinding.radioButtonWeeks,
                        dialogBinding.radioButtonWeeks.isChecked)
                    monthsButtonListener.onCheckedChanged(
                        dialogBinding.radioButtonMonths,
                        dialogBinding.radioButtonMonths.isChecked)
                }
            })
        dialogBinding.radioButtonDays.setOnCheckedChangeListener(daysButtonListener)
        dialogBinding.radioButtonWeeks.setOnCheckedChangeListener(weeksButtonListener)
        dialogBinding.radioButtonMonths.setOnCheckedChangeListener(monthsButtonListener)
    }
}