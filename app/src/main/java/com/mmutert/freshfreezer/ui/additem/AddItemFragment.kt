package com.mmutert.freshfreezer.ui.additem

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.mmutert.freshfreezer.EventObserver
import com.mmutert.freshfreezer.R
import com.mmutert.freshfreezer.data.AmountUnit
import com.mmutert.freshfreezer.data.Condition
import com.mmutert.freshfreezer.databinding.FragmentAddItemBinding
import com.mmutert.freshfreezer.ui.ConditionArrayAdapter
import com.mmutert.freshfreezer.ui.UnitArrayAdapter
import com.mmutert.freshfreezer.ui.dialogs.NotificationOffsetDialogFragment
import com.mmutert.freshfreezer.ui.dialogs.NotificationOffsetDialogFragment.NotificationOffsetDialogClickListener
import com.mmutert.freshfreezer.util.Keyboard
import com.mmutert.freshfreezer.util.getViewModelFactory
import com.mmutert.freshfreezer.util.setupSnackbar
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import java.util.*

class AddItemFragment : Fragment() {
    private val viewModel: AddItemViewModel by viewModels { getViewModelFactory() }
    private val args by navArgs<AddItemFragmentArgs>()

    private lateinit var binding: FragmentAddItemBinding
    private lateinit var mNotificationListAdapter: NotificationListAdapter

    private lateinit var spinnerUnitAdapter: UnitArrayAdapter
    private lateinit var conditionSpinnerAdapter: ConditionArrayAdapter

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentAddItemBinding.inflate(inflater, container, false)
        setHasOptionsMenu(false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val itemId = args.itemId
        viewModel.start(itemId)


        binding.apply {
            viewModel = this@AddItemFragment.viewModel
            lifecycleOwner = viewLifecycleOwner
        }

        mNotificationListAdapter = NotificationListAdapter(requireContext(), viewModel)
        viewModel.notifications.observe(viewLifecycleOwner) {
            mNotificationListAdapter.setItems(it)
        }

        binding.rvAddItemNotificationList.apply {
            adapter = mNotificationListAdapter
        }

        setupSnackbar()
        setUpFloatingActionButton()
        setUpAddNotificationDialog()
        setUpUnitSpinner()
        setupConditionSpinner()
        setupNavigation()
        setupFreezingDatePicker()
        setupBestBeforeDatePicker()
    }


    /**
     * Shows the freezing date entry in the fragment.
     */
    private fun showFreezingDate() {
        binding.tvAddFreezingDate.visibility = View.GONE
        binding.rlFreezingDateLayout.visibility = View.VISIBLE
    }

    /**
     * Sets up the spinner for the selected unit of the item amount
     */
    private fun setUpUnitSpinner() {
        spinnerUnitAdapter = UnitArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item
        )
        binding.spAddItemsUnitSelection.adapter = spinnerUnitAdapter
        binding.spAddItemsUnitSelection.onItemSelectedListener =
                object : OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?,
                                                view: View,
                                                position: Int,
                                                id: Long) {
                        val selectedUnit = spinnerUnitAdapter.getSelectedUnit(position)
                        viewModel.setAmountUnit(selectedUnit)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        viewModel.setAmountUnit(AmountUnit.GRAMS)
                    }
                }

        viewModel.selectedUnit.observe(viewLifecycleOwner) {
            val indexOfUnit = spinnerUnitAdapter.getIndexOfUnit(it)
            binding.spAddItemsUnitSelection.setSelection(indexOfUnit)
        }
    }

    private fun setupConditionSpinner() {
        conditionSpinnerAdapter = ConditionArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item
        )
        binding.apply {
            spAddItemCondition.adapter = conditionSpinnerAdapter
            spAddItemCondition.onItemSelectedListener = object : OnItemSelectedListener {

                override fun onItemSelected(parent: AdapterView<*>?,
                                            view: View,
                                            position: Int,
                                            id: Long) {
                    val selectedCondition = conditionSpinnerAdapter.getSelectedUnit(position)
                    this@AddItemFragment.viewModel.setCondition(selectedCondition)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    this@AddItemFragment.viewModel.setCondition(Condition.ROOM_TEMP)
                }
            }
        }

        viewModel.selectedCondition.observe(viewLifecycleOwner) {
            val indexOfUnit = conditionSpinnerAdapter.getIndexOfUnit(it)
            binding.spAddItemCondition.setSelection(indexOfUnit)
        }
    }

    private fun setUpAddNotificationDialog() {
        viewModel.addNotificationEvent.observe(viewLifecycleOwner, EventObserver {
            // Open notification dialog
            NotificationOffsetDialogFragment(object : NotificationOffsetDialogClickListener {
                override fun onPositiveClick(dialog: NotificationOffsetDialogFragment?) {
                    dialog?.let {
                        viewModel.addNotification(dialog.enteredOffset, dialog.offSetAmount)
                    }
                }
            }).show(parentFragmentManager, "add notification")
        })
    }


    /**
     * Creates a material date picker. This can be shown using result.show()
     *
     * @param titleStringId        The string id of the title text
     * @param defaultSelectionDate The date
     * @return The date picker
     */
    private fun createDatePicker(
            titleStringId: Int,
            defaultSelectionDate: LocalDate
    ): MaterialDatePicker<Long> {
        return MaterialDatePicker.Builder
            .datePicker()
            .setTitleText(resources.getString(titleStringId))
            // TODO Fix hack with adding hours to get the correct selection
            .setSelection(
                defaultSelectionDate
                    .toLocalDateTime(LocalTime.MIDNIGHT.plusHours(12))
                    .toDate()
                    .time
            )
            .build()
    }

    /**
     * Sets up the date picker dialogs for the date of freezing the item and the best before date.
     */
    private fun setupFreezingDatePicker() {
        viewModel.frozenDate.observe(viewLifecycleOwner) {
            // Set up the freezing date picker
            val picker = createDatePicker(
                R.string.add_item_frozen_at_date_picker_title_text, it
            )

            // Add the behavior for the positive button of the freezing date picker
            picker.addOnPositiveButtonClickListener { selection: Long ->
                viewModel.setFrozenDate(convertSelectedDate(selection))
            }

            binding.etAddItemFrozenDate.setOnClickListener {
                // Open the date picker on clicking the row
                picker.show(parentFragmentManager, "FreezingDate")
            }
            binding.rlFreezingDateLayout.setOnClickListener {
                picker.show(parentFragmentManager, "FreezingDate")
            }
            binding.tvAddFreezingDate.setOnClickListener {
                showFreezingDate()
                picker.show(parentFragmentManager, "FreezingDate")
            }
        }
    }

    private fun setupBestBeforeDatePicker() {
        viewModel.bestBeforeDate.observe(viewLifecycleOwner) {
            val picker = createDatePicker(
                R.string.add_item_best_before_date_picker_title_text, it
            )

            picker.addOnPositiveButtonClickListener { selection: Long ->
                viewModel.setBestBefore(convertSelectedDate(selection))
            }

            // Open the date picker on clicking the row
            binding.etAddItemBestBeforeDate.setOnClickListener {
                picker.show(parentFragmentManager, "BestBeforeDate")
            }
            binding.rlBestBeforeDateLayout.setOnClickListener {
                picker.show(parentFragmentManager, "BestBeforeDate")
            }
        }
    }

    /**
     * Converts the selected date from a date picker to a [LocalDate].
     *
     * @param selection The selected date from the date picker
     * @return The selected date converted to LocalDate.
     */
    private fun convertSelectedDate(selection: Long): LocalDate {
        return LocalDate.fromDateFields(Date(selection))
    }

    private fun setupNavigation() {
        viewModel.personSaved.observe(viewLifecycleOwner) {
            // TODO Add navigation parameter to indicate that a person has been saved/updated
            Keyboard.hideKeyboardFrom(requireActivity(), requireView())
            findNavController().navigateUp()
        }
    }

    private fun setupSnackbar() {
        view?.setupSnackbar(this, viewModel.snackbarTextId, Snackbar.LENGTH_SHORT)

        viewModel.snackbarTextId.observe(viewLifecycleOwner, EventObserver {
            viewModel.showSnackbarMessage(it)
        })
    }

    /**
     * Sets up the Floating Action Button for saving the new item
     */
    private fun setUpFloatingActionButton() {
        binding.floatingActionButton.setOnClickListener {
            Log.d(LOG_TAG, "Clicked on Save button")

            var noErrors = true

            for (textInputLayout in listOf(binding.tilAddItemName)) {
                val editTextString = textInputLayout.editText!!.text.toString()
                if (editTextString.isBlank()) {
                    textInputLayout.error = resources.getString(R.string.error_string)
                    noErrors = false
                } else {
                    textInputLayout.error = null
                }
            }

            if (noErrors) {
                viewModel.save()
            }
        }
    }

    companion object {
        val LOG_TAG = AddItemFragment::class.simpleName
    }
}