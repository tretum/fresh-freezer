package com.mmutert.freshfreezer.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.mmutert.freshfreezer.R
import com.mmutert.freshfreezer.data.*
import com.mmutert.freshfreezer.databinding.FragmentAddItemBinding
import com.mmutert.freshfreezer.ui.AddItemFragment.NotificationListAdapter.NotificationListAdapterViewHolder
import com.mmutert.freshfreezer.ui.NotificationOffsetDialogFragment.NotificationOffsetDialogClickListener
import com.mmutert.freshfreezer.util.Keyboard
import com.mmutert.freshfreezer.util.TimeHelper.currentDateLocalized
import com.mmutert.freshfreezer.viewmodel.AddItemViewModel
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import java.util.*

class AddItemFragment : Fragment() {
    private lateinit var addItemViewModel: AddItemViewModel
    private lateinit var mBinding: FragmentAddItemBinding
    private var freezingDatePicker: MaterialDatePicker<Long>? = null
    private lateinit var spinnerUnitAdapter: UnitArrayAdapter
    private lateinit var conditionSpinnerAdapter: ConditionArrayAdapter
    private var mNotificationListAdapter: NotificationListAdapter? = null
    private var bestBeforeDatePicker: MaterialDatePicker<Long>? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
        addItemViewModel = ViewModelProvider(requireActivity()).get(
            AddItemViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mBinding = FragmentAddItemBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null || !savedInstanceState.getBoolean("editing")) {
            // This is not a recreation of the fragment due to rotation or something similar
            // Therefore we either need to load the item in the model or prepare for a new item
            val itemId = AddItemFragmentArgs.fromBundle(requireArguments()).itemId
            if (itemId == -1L) {
                // No item to edit => Prepare for new item
                addItemViewModel.reset()
            } else {
                // Prepare to edit the requested item
                addItemViewModel
                    .getItemAndNotifications(itemId)
                    .observe(viewLifecycleOwner, { itemAndNotifications: ItemAndNotifications ->
                        addItemViewModel.currentItem = itemAndNotifications.item
                        addItemViewModel.notifications = itemAndNotifications.notifications.toMutableList()
                        mNotificationListAdapter!!.setItems(addItemViewModel.currentNotifications)
                        updateWithNewData()
                    })
            }
        }
        mBinding.currentItem = addItemViewModel.currentItem
        mNotificationListAdapter = NotificationListAdapter()
        mBinding.rvAddItemNotificationList.adapter = mNotificationListAdapter
        mBinding.rvAddItemNotificationList.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.VERTICAL,
            false
        )
        setUpFloatingActionButton()
        setUpDatePickers()
        setUpFreezingDateButton()
        setUpAddNotificationButton()
        setUpUnitSpinner()
        setUpConditionSpinner()
        updateWithNewData()
    }

    /**
     * Updates the Interface elements with the data from the current item of the view model.
     */
    private fun updateWithNewData() {
        val item = addItemViewModel.currentItem
        mBinding.currentItem = item

        // Spinners
        mBinding.spAddItemsUnitSelection.setSelection(spinnerUnitAdapter.getIndexOfUnit(item.unit))
        mBinding.spAddItemCondition.setSelection(conditionSpinnerAdapter.getIndexOfUnit(item.condition))
        setUpDatePickers()
    }

    /**
     * Shows the freezing date entry in the fragment.
     */
    private fun showFreezingDate() {
        mBinding.tvAddFreezingDate.visibility = View.GONE
        mBinding.rlFreezingDateLayout.visibility = View.VISIBLE
    }

    /**
     * Hides all UI elements regarding the freezing date
     */
    private fun hideFreezingDateElements() {
        mBinding.tvAddFreezingDate.visibility = View.GONE
        mBinding.rlFreezingDateLayout.visibility = View.GONE
    }

    /**
     * Hides the freezing date and shows a button for adding a freezing date instead.
     */
    private fun showAddFreezingDateButton() {
        mBinding.tvAddFreezingDate.visibility = View.VISIBLE
        mBinding.rlFreezingDateLayout.visibility = View.GONE
    }

    /**
     * Sets up the spinner for the selected unit of the item amount
     */
    private fun setUpUnitSpinner() {
        spinnerUnitAdapter = UnitArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item
        )
        mBinding.spAddItemsUnitSelection.adapter = spinnerUnitAdapter
        mBinding.spAddItemsUnitSelection.onItemSelectedListener =
                object : OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?,
                                                view: View,
                                                position: Int,
                                                id: Long) {
                        val selectedUnit = spinnerUnitAdapter.getSelectedUnit(position)
                        addItemViewModel.currentItem.unit = selectedUnit
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        addItemViewModel.currentItem.unit = AmountUnit.GRAMS
                    }
                }
    }

    private fun setUpConditionSpinner() {
        conditionSpinnerAdapter = ConditionArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item
        )
        mBinding.spAddItemCondition.adapter = conditionSpinnerAdapter
        mBinding.spAddItemCondition.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?,
                                        view: View,
                                        position: Int,
                                        id: Long) {
                val selectedCondition = conditionSpinnerAdapter.getSelectedUnit(position)
                when (selectedCondition) {
                    Condition.FROZEN -> showAddFreezingDateButton()
                    Condition.CHILLED, Condition.ROOM_TEMP -> hideFreezingDateElements()
                }
                addItemViewModel.currentItem.condition = selectedCondition
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                addItemViewModel.currentItem.condition = Condition.ROOM_TEMP
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("editing", true)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setUpAddNotificationButton() {
        mBinding.tvAddNotification.setOnClickListener { v: View? ->

            // Open notification dialog
            NotificationOffsetDialogFragment(object : NotificationOffsetDialogClickListener {
                override fun onPositiveClick(dialog: NotificationOffsetDialogFragment?) {
                    dialog?.let {
                        Log.d(TAG, "Selected notification offset from dialog.")
                        val offSetUnitTime = dialog.offSetAmount
                        val enteredOffset = dialog.enteredOffset
                        Log.d(TAG, "Selected Offset: $enteredOffset")
                        Log.d(TAG, "Selected Unit: $offSetUnitTime")
                        val notification =
                                addItemViewModel.addNotification(enteredOffset, offSetUnitTime)
                        if (notification != null) {
                            mNotificationListAdapter!!.addNotificationEntry(notification)
                        } else {
                            Log.d(
                                TAG,
                                "The selected offset already exists. Not adding a second copy.")
                        }
                    }
                }
            }).show(parentFragmentManager, "add notification")
        }
    }

    private fun setUpFreezingDateButton() {
        mBinding.tvAddFreezingDate.setOnClickListener { v: View? ->
            showFreezingDate()
            freezingDatePicker!!.show(parentFragmentManager, freezingDatePicker.toString())
        }
    }

    /**
     * Creates a material date picker. This can be shown using result.show()
     *
     * @param titleStringId        The string id of the title text
     * @param defaultSelectionDate The date
     * @return The date picker
     */
    private fun createDatePicker(titleStringId: Int,
                                 defaultSelectionDate: LocalDate?): MaterialDatePicker<Long> {
        val builder = MaterialDatePicker.Builder.datePicker()
        builder.setTitleText(resources.getString(titleStringId))
        // TODO Fix hack with adding hours to get the correct selection
        builder.setSelection(
            defaultSelectionDate!!.toLocalDateTime(LocalTime.MIDNIGHT.plusHours(12))
                .toDate().time)
        return builder.build()
    }

    /**
     * Sets up the date picker dialogs for the date of freezing the item and the best before date.
     */
    private fun setUpDatePickers() {
        val currentDate = currentDateLocalized
        val item = addItemViewModel.currentItem

        // Set up the frozen date picker
        mBinding.etAddItemFrozenDate.inputType = InputType.TYPE_NULL
        if (item.frozenAtDate != null) {
            mBinding.etAddItemFrozenDate.setText(
                addItemViewModel.DATE_FORMATTER.print(item.frozenAtDate)
            )
            showFreezingDate()
        } else {
            mBinding.etAddItemFrozenDate.setText(
                addItemViewModel.DATE_FORMATTER.print(
                    currentDate))
        }

        // Set up the freezing date picker
        freezingDatePicker = createDatePicker(
            R.string.add_item_frozen_at_date_picker_title_text,
            if (addItemViewModel.currentItem.frozenAtDate != null) addItemViewModel.currentItem.frozenAtDate else currentDate
        )

        // Add the behavior for the positive button of the freezing date picker
        freezingDatePicker!!.addOnPositiveButtonClickListener { selection: Long ->
            val date = convertSelectedDate(selection)
            item.frozenAtDate = date
            val selectedFrozenDateFormatted = addItemViewModel.DATE_FORMATTER.print(date)
            mBinding.etAddItemFrozenDate.setText(selectedFrozenDateFormatted)
        }

        // Open the freezing date picker on clicking the row
        mBinding.etAddItemFrozenDate.setOnClickListener { v: View? ->
            freezingDatePicker!!.show(
                parentFragmentManager, freezingDatePicker.toString())
        }
        mBinding.rlFreezingDateLayout.setOnClickListener { v: View? ->
            freezingDatePicker!!.show(
                parentFragmentManager, freezingDatePicker.toString())
        }
        val bestBeforeDateFormatted = addItemViewModel.DATE_FORMATTER.print(item.bestBeforeDate)
        mBinding.etAddItemBestBefore.setText(bestBeforeDateFormatted)
        mBinding.etAddItemBestBefore.inputType = InputType.TYPE_NULL
        bestBeforeDatePicker = createDatePicker(
            R.string.add_item_best_before_date_picker_title_text,
            item.bestBeforeDate
        )
        bestBeforeDatePicker!!.addOnPositiveButtonClickListener { selection: Long ->
            val date = convertSelectedDate(selection)
            addItemViewModel.updateBestBefore(date)
            mNotificationListAdapter!!.setItems(addItemViewModel.currentNotifications)
            val selectedFrozenDateFormatted = addItemViewModel.DATE_FORMATTER.print(date)
            mBinding.etAddItemBestBefore.setText(selectedFrozenDateFormatted)
        }
        mBinding.etAddItemBestBefore.setOnClickListener { v: View? ->
            bestBeforeDatePicker!!.show(
                parentFragmentManager, bestBeforeDatePicker.toString())
        }
        mBinding.rlBestBeforeDateLayout.setOnClickListener { v: View? ->
            bestBeforeDatePicker!!.show(
                parentFragmentManager, bestBeforeDatePicker.toString())
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

    /**
     * Sets up the Floating Action Button for saving the new item
     */
    private fun setUpFloatingActionButton() {
        mBinding.floatingActionButton.setOnClickListener { fab: View? ->
            // TODO Check validity of inputs
            Log.d("AddItem", "Clicked on Save button")
            val (_, _, _, _, frozenAtDate, bestBeforeDate) = addItemViewModel.currentItem
            var invalidInput = false

            // Input Check: The best before date should not be after the freezing date, if that is specified
            if (frozenAtDate != null && frozenAtDate.isAfter(bestBeforeDate)) {
                Snackbar.make(
                    requireView(),
                    R.string.add_item_bbd_before_freezing_date_error,
                    Snackbar.LENGTH_SHORT)
                    .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
                    .show()
                invalidInput = true
            }
            if (bestBeforeDate.isBefore(currentDateLocalized)) {
                Snackbar.make(
                    requireView(),
                    R.string.add_item_bbd_before_current_date_error,
                    Snackbar.LENGTH_SHORT)
                    .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
                    .show()
                invalidInput = true
            }
            if (!invalidInput) {
                addItemViewModel.save()
                Keyboard.hideKeyboardFrom(requireActivity(), fab!!)
                Navigation.findNavController(fab).navigate(R.id.action_new_item_save)
            }
        }
    }

    /**
     * The Adapter for the list of pending or created notifications.
     */
    private inner class NotificationListAdapter : RecyclerView.Adapter<NotificationListAdapterViewHolder>() {
        fun setItems(notificationList: List<ItemNotification>) {
            mDiffer.submitList(ArrayList(notificationList))
        }

        fun addNotificationEntry(notification: ItemNotification) {
            Log.d(TAG, "Adding notification entry to RV")
            val notifications = ArrayList(mDiffer.currentList)
            notifications.add(notification)
            mDiffer.submitList(notifications)
        }

        private fun removeNotificationEntry(notification: ItemNotification) {
            Log.d(TAG, "Removing notification entry from RV")
            val notifications = ArrayList(mDiffer.currentList)
            notifications.remove(notification)
            mDiffer.submitList(notifications)
        }

        /**
         * The Callback for the DiffUtil.
         */
        private val DIFF_CALLBACK: DiffUtil.ItemCallback<ItemNotification> =
                object : DiffUtil.ItemCallback<ItemNotification>() {
                    override fun areItemsTheSame(
                            oldNotification: ItemNotification,
                            newNotification: ItemNotification): Boolean {
                        // Notification properties may have changed if reloaded from the DB, but ID is fixed
                        // TODO Fix
                        return oldNotification.offsetAmount == newNotification.offsetAmount
                                && (oldNotification.timeOffsetUnit == newNotification.timeOffsetUnit)
                    }

                    override fun areContentsTheSame(
                            oldNotification: ItemNotification,
                            newNotification: ItemNotification): Boolean {
                        // NOTE: if you use equals, your object must properly override Object#equals()
                        // Incorrectly returning false here will result in too many animations.
                        return oldNotification == newNotification
                    }
                }
        private val mDiffer = AsyncListDiffer(this, DIFF_CALLBACK)
        override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int): NotificationListAdapterViewHolder {
            val view = layoutInflater.inflate(R.layout.notification_entry, parent, false)
            return NotificationListAdapterViewHolder(view)
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onBindViewHolder(holder: NotificationListAdapterViewHolder, position: Int) {
            val notification = getNotificationAtPosition(position)
            val timeOffsetUnit = notification.timeOffsetUnit
            val offsetAmount = notification.offsetAmount
            when (timeOffsetUnit) {
                TimeOffsetUnit.DAYS -> holder.entry.text = resources.getQuantityString(
                    R.plurals.notification_list_entry_days_before_capitalized,
                    offsetAmount,
                    offsetAmount
                )
                TimeOffsetUnit.WEEKS -> holder.entry.text = resources.getQuantityString(
                    R.plurals.notification_list_entry_weeks_before_capitalized,
                    offsetAmount,
                    offsetAmount
                )
                TimeOffsetUnit.MONTHS -> holder.entry.text = resources.getQuantityString(
                    R.plurals.notification_list_entry_months_before_capitalized,
                    offsetAmount,
                    offsetAmount
                )
            }

            // Add the delete button for the notification in the list
            holder.entry.setOnTouchListener { v1: View?, event: MotionEvent ->
                val DRAWABLE_LEFT = 0
                val DRAWABLE_TOP = 1
                val DRAWABLE_RIGHT = 2
                val DRAWABLE_BOTTOM = 3
                if (event.action == MotionEvent.ACTION_DOWN) {
                    val rightDrawablePositionX = holder.entry.right
                    val rightDrawableWidth = holder.entry.compoundDrawables[DRAWABLE_RIGHT]
                        .bounds
                        .width()
                    if (event.rawX >= rightDrawablePositionX - rightDrawableWidth) {
                        // Remove notification from list
                        addItemViewModel.addNotificationToDelete(notification)
                        removeNotificationEntry(notification)
                        return@setOnTouchListener true
                    }
                }
                false
            }
        }

        private fun getNotificationAtPosition(position: Int): ItemNotification {
            return mDiffer.currentList[position]
        }

        override fun getItemCount(): Int {
            return mDiffer.currentList.size
        }

        /**
         * The view holder for the notification list recycler view
         */
        private inner class NotificationListAdapterViewHolder(itemView: View) :
                RecyclerView.ViewHolder(itemView) {
            val entry: TextView = itemView as TextView
        }
    }

    companion object {
        const val TAG = "AddItemFragment"
    }
}