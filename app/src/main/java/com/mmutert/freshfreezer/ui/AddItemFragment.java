package com.mmutert.freshfreezer.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;
import com.mmutert.freshfreezer.R;
import com.mmutert.freshfreezer.data.AmountUnit;
import com.mmutert.freshfreezer.data.Condition;
import com.mmutert.freshfreezer.data.FrozenItem;
import com.mmutert.freshfreezer.data.ItemNotification;
import com.mmutert.freshfreezer.data.TimeOffsetUnit;
import com.mmutert.freshfreezer.databinding.FragmentAddItemBinding;
import com.mmutert.freshfreezer.util.Keyboard;
import com.mmutert.freshfreezer.util.TimeHelper;
import com.mmutert.freshfreezer.viewmodel.AddItemViewModel;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class AddItemFragment extends Fragment {

    public static final String TAG = "AddItemFragment";

    private AddItemViewModel addItemViewModel;

    private FragmentAddItemBinding mBinding;
    private MaterialDatePicker<Long> freezingDatePicker;
    private UnitArrayAdapter spinnerUnitAdapter;
    private ConditionArrayAdapter conditionSpinnerAdapter;
    private NotificationListAdapter mNotificationListAdapter;
    private MaterialDatePicker<Long> bestBeforeDatePicker;


    public AddItemFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
        this.addItemViewModel = new ViewModelProvider(requireActivity()).get(AddItemViewModel.class);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBinding = FragmentAddItemBinding.inflate(inflater, container, false);

        return mBinding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState == null || !savedInstanceState.getBoolean("editing")) {
            // This is not a recreation of the fragment due to rotation or something similar
            // Therefore we either need to load the item in the model or prepare for a new item

            long itemId = AddItemFragmentArgs.fromBundle(getArguments()).getItemId();
            if (itemId == -1) {
                // No item to edit => Prepare for new item
                addItemViewModel.reset();
            } else {
                // Prepare to edit the requested item
                addItemViewModel
                        .getItemAndNotifications(itemId)
                        .observe(getViewLifecycleOwner(), itemAndNotifications -> {
                            addItemViewModel.setCurrentItem(itemAndNotifications.getItem());
                            addItemViewModel.setNotifications(itemAndNotifications.getNotifications());
                            mNotificationListAdapter.setItems(addItemViewModel.getCurrentNotifications());
                            updateWithNewData();
                        });
            }
        }

        mBinding.setCurrentItem(addItemViewModel.getCurrentItem());

        mNotificationListAdapter = new NotificationListAdapter();
        mBinding.rvAddItemNotificationList.setAdapter(mNotificationListAdapter);
        mBinding.rvAddItemNotificationList.setLayoutManager(new LinearLayoutManager(
                getContext(),
                RecyclerView.VERTICAL,
                false
        ));

        setUpFloatingActionButton();
        setUpDatePickers();
        setUpFreezingDateButton();
        setUpAddNotificationButton();
        setUpUnitSpinner();
        setUpConditionSpinner();

        updateWithNewData();
    }


    /**
     * Updates the Interface elements with the data from the current item of the view model.
     */
    private void updateWithNewData() {

        FrozenItem item = addItemViewModel.getCurrentItem();

        mBinding.setCurrentItem(item);

        // Spinners
        mBinding.spAddItemsUnitSelection.setSelection(spinnerUnitAdapter.getIndexOfUnit(item.getUnit()));
        mBinding.spAddItemCondition.setSelection(conditionSpinnerAdapter.getIndexOfUnit(item.getCondition()));

        setUpDatePickers();
    }


    /**
     * Shows the freezing date entry in the fragment.
     */
    private void showFreezingDate() {

        mBinding.tvAddFreezingDate.setVisibility(View.GONE);
        mBinding.rlFreezingDateLayout.setVisibility(View.VISIBLE);
    }


    /**
     * Hides all UI elements regarding the freezing date
     */
    private void hideFreezingDateElements() {

        mBinding.tvAddFreezingDate.setVisibility(View.GONE);
        mBinding.rlFreezingDateLayout.setVisibility(View.GONE);
    }


    /**
     * Hides the freezing date and shows a button for adding a freezing date instead.
     */
    private void showAddFreezingDateButton() {

        mBinding.tvAddFreezingDate.setVisibility(View.VISIBLE);
        mBinding.rlFreezingDateLayout.setVisibility(View.GONE);
    }


    /**
     * Sets up the spinner for the selected unit of the item amount
     */
    private void setUpUnitSpinner() {

        spinnerUnitAdapter = new UnitArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item
        );
        mBinding.spAddItemsUnitSelection.setAdapter(spinnerUnitAdapter);
        mBinding.spAddItemsUnitSelection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                AmountUnit selectedUnit = spinnerUnitAdapter.getSelectedUnit(position);
                addItemViewModel.getCurrentItem().setUnit(selectedUnit);
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {

                addItemViewModel.getCurrentItem().setUnit(AmountUnit.GRAMS);
            }
        });
    }


    private void setUpConditionSpinner() {

        conditionSpinnerAdapter = new ConditionArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item
        );
        mBinding.spAddItemCondition.setAdapter(conditionSpinnerAdapter);


        mBinding.spAddItemCondition.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                Condition selectedCondition = conditionSpinnerAdapter.getSelectedUnit(position);

                switch (selectedCondition) {
                    case FROZEN:
                        showAddFreezingDateButton();
                        break;
                    case CHILLED:
                    case ROOM_TEMP:
                        hideFreezingDateElements();
                        break;
                }

                addItemViewModel.getCurrentItem().setCondition(selectedCondition);
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {

                addItemViewModel.getCurrentItem().setCondition(Condition.ROOM_TEMP);
            }
        });
    }


    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {

        super.onSaveInstanceState(outState);
        outState.putBoolean("editing", true);
    }


    @SuppressLint("ClickableViewAccessibility")
    private void setUpAddNotificationButton() {

        mBinding.tvAddNotification.setOnClickListener(v -> {

            // Open notification dialog
            new NotificationOffsetDialogFragment(dialog -> {
                Log.d(TAG, "Selected notification offset from dialog.");

                TimeOffsetUnit offSetUnitTime = dialog.getOffSetAmount();
                int enteredOffset = dialog.getEnteredOffset();

                Log.d(TAG, "Selected Offset: " + enteredOffset);
                Log.d(TAG, "Selected Unit: " + offSetUnitTime);

                ItemNotification notification = addItemViewModel.addNotification(enteredOffset, offSetUnitTime);
                if (notification != null) {
                    mNotificationListAdapter.addNotificationEntry(notification);
                }
                {
                    Log.d(TAG, "The selected offset already exists. Not adding a second copy.");
                }
            }).show(getParentFragmentManager(), "add notification");
        });
    }


    private void setUpFreezingDateButton() {

        mBinding.tvAddFreezingDate.setOnClickListener(v -> {
            showFreezingDate();
            freezingDatePicker.show(getParentFragmentManager(), freezingDatePicker.toString());
        });
    }


    /**
     * Creates a material date picker. This can be shown using result.show()
     *
     * @param titleStringId        The string id of the title text
     * @param defaultSelectionDate The date
     * @return The date picker
     */
    private MaterialDatePicker<Long> createDatePicker(int titleStringId, LocalDate defaultSelectionDate) {

        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText(getResources().getString(titleStringId));
        // TODO Fix hack with adding hours to get the correct selection
        builder.setSelection(defaultSelectionDate.toLocalDateTime(LocalTime.MIDNIGHT.plusHours(12)).toDate().getTime());
        return builder.build();
    }


    /**
     * Sets up the date picker dialogs for the date of freezing the item and the best before date.
     */
    private void setUpDatePickers() {

        LocalDate currentDate = TimeHelper.getCurrentDateLocalized();
        FrozenItem item = addItemViewModel.getCurrentItem();

        // Set up the frozen date picker
        mBinding.etAddItemFrozenDate.setInputType(InputType.TYPE_NULL);
        if (item.getFrozenAtDate() != null) {
            mBinding.etAddItemFrozenDate.setText(
                    addItemViewModel.DATE_FORMATTER.print(item.getFrozenAtDate())
            );
            showFreezingDate();
        } else {
            mBinding.etAddItemFrozenDate.setText(addItemViewModel.DATE_FORMATTER.print(currentDate));
        }

        // Set up the freezing date picker
        freezingDatePicker = createDatePicker(
                R.string.add_item_frozen_at_date_picker_title_text,
                addItemViewModel.getCurrentItem().getFrozenAtDate() != null ?
                        addItemViewModel.getCurrentItem().getFrozenAtDate() : currentDate
        );

        // Add the behavior for the positive button of the freezing date picker
        freezingDatePicker.addOnPositiveButtonClickListener(selection -> {
            LocalDate date = convertSelectedDate(selection);
            item.setFrozenAtDate(date);
            String selectedFrozenDateFormatted = addItemViewModel.DATE_FORMATTER.print(date);
            mBinding.etAddItemFrozenDate.setText(selectedFrozenDateFormatted);
        });

        // Open the freezing date picker on clicking the row
        mBinding.etAddItemFrozenDate.setOnClickListener(v -> {
            freezingDatePicker.show(getParentFragmentManager(), freezingDatePicker.toString());
        });
        mBinding.rlFreezingDateLayout.setOnClickListener(v -> {
            freezingDatePicker.show(getParentFragmentManager(), freezingDatePicker.toString());
        });

        String bestBeforeDateFormatted =
                addItemViewModel.DATE_FORMATTER.print(item.getBestBeforeDate());
        mBinding.etAddItemBestBefore.setText(bestBeforeDateFormatted);
        mBinding.etAddItemBestBefore.setInputType(InputType.TYPE_NULL);

        bestBeforeDatePicker = createDatePicker(
                R.string.add_item_best_before_date_picker_title_text,
                item.getBestBeforeDate()
        );

        bestBeforeDatePicker.addOnPositiveButtonClickListener(selection -> {
            LocalDate date = convertSelectedDate(selection);
            addItemViewModel.updateBestBefore(date);
            mNotificationListAdapter.setItems(addItemViewModel.getCurrentNotifications());
            String selectedFrozenDateFormatted = addItemViewModel.DATE_FORMATTER.print(date);
            mBinding.etAddItemBestBefore.setText(selectedFrozenDateFormatted);
        });

        mBinding.etAddItemBestBefore.setOnClickListener(v -> {
            bestBeforeDatePicker.show(getParentFragmentManager(), bestBeforeDatePicker.toString());
        });
        mBinding.rlBestBeforeDateLayout.setOnClickListener(v -> {
            bestBeforeDatePicker.show(getParentFragmentManager(), bestBeforeDatePicker.toString());
        });
    }


    /**
     * Converts the selected date from a date picker to a {@link LocalDate}.
     *
     * @param selection The selected date from the date picker
     * @return The selected date converted to LocalDate.
     */
    private LocalDate convertSelectedDate(final Long selection) {

        return LocalDate.fromDateFields(new Date(selection));
    }


    /**
     * Sets up the Floating Action Button for saving the new item
     */
    private void setUpFloatingActionButton() {

        mBinding.floatingActionButton.setOnClickListener(fab -> {
            // TODO Check validity of inputs
            Log.d("AddItem", "Clicked on Save button");
            FrozenItem item = addItemViewModel.getCurrentItem();

            boolean invalidInput = false;

            // Input Check: The best before date should not be after the freezing date, if that is specified
            if (item.getFrozenAtDate() != null && item.getFrozenAtDate().isAfter(item.getBestBeforeDate())) {
                Snackbar.make(requireView(), R.string.add_item_bbd_before_freezing_date_error, Snackbar.LENGTH_SHORT)
                        .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
                        .show();
                invalidInput = true;
            }

            if (item.getBestBeforeDate().isBefore(TimeHelper.getCurrentDateLocalized())) {
                Snackbar.make(requireView(), R.string.add_item_bbd_before_current_date_error, Snackbar.LENGTH_SHORT)
                        .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
                        .show();
                invalidInput = true;
            }


            if (!invalidInput) {
                addItemViewModel.save();

                Keyboard.hideKeyboardFrom(requireActivity(), fab);
                Navigation.findNavController(fab).navigate(R.id.action_new_item_save);
            }
        });
    }


    /**
     * The Adapter for the list of pending or created notifications.
     */
    private class NotificationListAdapter
            extends RecyclerView.Adapter<NotificationListAdapter.NotificationListAdapterViewHolder> {


        public NotificationListAdapter() {

        }


        private void setItems(List<ItemNotification> notificationList) {

            mDiffer.submitList(new ArrayList<>(notificationList));
        }


        private void addNotificationEntry(ItemNotification notification) {

            Log.d(TAG, "Adding notification entry to RV");
            ArrayList<ItemNotification> notifications = new ArrayList<>(mDiffer.getCurrentList());
            notifications.add(notification);
            mDiffer.submitList(notifications);
        }


        private void removeNotificationEntry(final ItemNotification notification) {

            Log.d(TAG, "Removing notification entry from RV");
            ArrayList<ItemNotification> notifications = new ArrayList<>(mDiffer.getCurrentList());
            notifications.remove(notification);
            mDiffer.submitList(notifications);
        }


        /**
         * The Callback for the DiffUtil.
         */
        private final DiffUtil.ItemCallback<ItemNotification> DIFF_CALLBACK
                = new DiffUtil.ItemCallback<ItemNotification>() {
            @Override
            public boolean areItemsTheSame(
                    @NonNull ItemNotification oldNotification, @NonNull ItemNotification newNotification) {
                // Notification properties may have changed if reloaded from the DB, but ID is fixed
                // TODO Fix
                return oldNotification.getOffsetAmount() == newNotification.getOffsetAmount() && oldNotification
                        .getTimeOffsetUnit()
                        .equals(newNotification.getTimeOffsetUnit());
            }


            @Override
            public boolean areContentsTheSame(
                    @NonNull ItemNotification oldNotification, @NonNull ItemNotification newNotification) {
                // NOTE: if you use equals, your object must properly override Object#equals()
                // Incorrectly returning false here will result in too many animations.
                return oldNotification.equals(newNotification);
            }
        };
        private final AsyncListDiffer<ItemNotification> mDiffer = new AsyncListDiffer<>(this, DIFF_CALLBACK);


        @NonNull
        @Override
        public NotificationListAdapterViewHolder onCreateViewHolder(
                @NonNull final ViewGroup parent,
                final int viewType) {

            View view = getLayoutInflater().inflate(R.layout.notification_entry, parent, false);
            return new NotificationListAdapterViewHolder(view);
        }


        @SuppressLint("ClickableViewAccessibility")
        @Override
        public void onBindViewHolder(@NonNull final NotificationListAdapterViewHolder holder, final int position) {

            ItemNotification notification = getNotificationAtPosition(position);
            TimeOffsetUnit timeOffsetUnit = notification.getTimeOffsetUnit();
            int offsetAmount = notification.getOffsetAmount();

            switch (timeOffsetUnit) {
                case DAYS:
                    holder.entry.setText(getResources().getQuantityString(
                            R.plurals.notification_list_entry_days_before_capitalized,
                            offsetAmount,
                            offsetAmount
                    ));
                    break;
                case WEEKS:
                    holder.entry.setText(getResources().getQuantityString(
                            R.plurals.notification_list_entry_weeks_before_capitalized,
                            offsetAmount,
                            offsetAmount
                    ));
                    break;
                case MONTHS:
                    holder.entry.setText(getResources().getQuantityString(
                            R.plurals.notification_list_entry_months_before_capitalized,
                            offsetAmount,
                            offsetAmount
                    ));
                    break;
            }

            // Add the delete button for the notification in the list
            holder.entry.setOnTouchListener((v1, event) -> {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    int rightDrawablePositionX = holder.getEntry().getRight();
                    int rightDrawableWidth = holder.getEntry().getCompoundDrawables()[DRAWABLE_RIGHT]
                            .getBounds()
                            .width();
                    if (event.getRawX() >= (rightDrawablePositionX - rightDrawableWidth)) {
                        // Remove notification from list
                        addItemViewModel.addNotificationToDelete(notification);
                        removeNotificationEntry(notification);
                        return true;
                    }
                }
                return false;
            });
        }


        private ItemNotification getNotificationAtPosition(int position) {

            return mDiffer.getCurrentList().get(position);
        }


        @Override
        public int getItemCount() {

            return mDiffer.getCurrentList().size();
        }


        /**
         * The view holder for the notification list recycler view
         */
        private class NotificationListAdapterViewHolder extends RecyclerView.ViewHolder {

            private final TextView entry;


            public NotificationListAdapterViewHolder(@NonNull final View itemView) {

                super(itemView);

                entry = (TextView) itemView;
            }


            public TextView getEntry() {

                return entry;
            }
        }
    }
}
