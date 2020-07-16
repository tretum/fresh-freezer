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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.mmutert.freshfreezer.R;
import com.mmutert.freshfreezer.data.AmountUnit;
import com.mmutert.freshfreezer.data.FrozenItem;
import com.mmutert.freshfreezer.data.ItemNotification;
import com.mmutert.freshfreezer.data.TimeOffsetUnit;
import com.mmutert.freshfreezer.databinding.FragmentAddItemBinding;
import com.mmutert.freshfreezer.util.Keyboard;
import com.mmutert.freshfreezer.viewmodel.AddItemViewModel;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class AddItemFragment extends Fragment {

    public static final String TAG = AddItemFragment.class.getName();

    private AddItemViewModel addItemViewModel;

    private FragmentAddItemBinding mBinding;
    private MaterialDatePicker<Long> freezingDatePicker;
    private UnitArrayAdapter spinnerUnitAdapter;
    private NotificationListAdapter mNotificationListAdapter;

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
            long itemId = AddItemFragmentArgs.fromBundle(getArguments()).getItemId();
            if (itemId == -1) {
                addItemViewModel.reset();
            } else {
                addItemViewModel
                        .getItemAndNotifications(itemId)
                        .observe(getViewLifecycleOwner(), itemAndNotifications -> {
                            addItemViewModel.setCurrentItem(itemAndNotifications.item);
                            addItemViewModel.setCurrentNotifications(itemAndNotifications.notifications);
                            mNotificationListAdapter.setItems(addItemViewModel.getCurrentNotifications());
                            updateWithNewData();
                        });
            }
        }

        mBinding.setCurrentItem(addItemViewModel.getItem());

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

        updateWithNewData();
    }

    /**
     * Updates the Interface elements with the data from the current item of the view model.
     */
    private void updateWithNewData() {
        FrozenItem item = addItemViewModel.getItem();

        mBinding.setCurrentItem(item);

        // Unit spinner
        mBinding.spAddItemsUnitSelection.setSelection(spinnerUnitAdapter.getIndexOfUnit(item.getUnit()));

        // TODO Notifications

        LocalDate frozenAtDate = item.getFrozenAtDate();
        if (frozenAtDate != null) {
            showFreezingDate();

            String selectedFrozenDateFormatted = addItemViewModel.DATE_FORMATTER.print(frozenAtDate);
            mBinding.etAddItemFrozenDate.setText(selectedFrozenDateFormatted);
        } else {
            showAddFreezingDateButton();
        }

        LocalDate bestBeforeDate = item.getBestBeforeDate();
        String bestBeforeDateFormatted = addItemViewModel.DATE_FORMATTER.print(bestBeforeDate);
        mBinding.etAddItemBestBefore.setText(bestBeforeDateFormatted);
    }

    private void showFreezingDate() {
        mBinding.tvAddFreezingDate.setVisibility(View.GONE);
        mBinding.rlFreezingDateLayout.setVisibility(View.VISIBLE);
    }

    private void showAddFreezingDateButton() {
        mBinding.tvAddFreezingDate.setVisibility(View.VISIBLE);
        mBinding.rlFreezingDateLayout.setVisibility(View.GONE);
    }

    private void setUpUnitSpinner() {
        spinnerUnitAdapter = new UnitArrayAdapter(
                getContext(),
                android.R.layout.simple_spinner_dropdown_item
        );
        mBinding.spAddItemsUnitSelection.setAdapter(spinnerUnitAdapter);
        mBinding.spAddItemsUnitSelection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                AmountUnit selectedUnit = spinnerUnitAdapter.getSelectedUnit(position);
                addItemViewModel.setUnit(selectedUnit);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                addItemViewModel.setUnit(AmountUnit.GRAMS);
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

                // TODO Remove duplication of notifications in ViewModel and RecyclerView
                ItemNotification notification = addItemViewModel.addNotification(enteredOffset, offSetUnitTime);
                mNotificationListAdapter.addNotificationEntry(notification);
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
        // Set up the date pickers for the best before and frozen date fields
        // TODO Fix such that it uses the values from the present item and initialize the item with current date for the best before date

        LocalDate currentDate = LocalDate.now();
        if(addItemViewModel.getItem().getBestBeforeDate() == null) {
            addItemViewModel.getItem().setBestBeforeDate(currentDate);
        }


        mBinding.etAddItemFrozenDate.setInputType(InputType.TYPE_NULL);
        if(addItemViewModel.getItem().getFrozenAtDate() != null) {
            mBinding.etAddItemFrozenDate.setText(
                    addItemViewModel.DATE_FORMATTER.print(addItemViewModel.getItem().getFrozenAtDate())
            );
        }
        mBinding.etAddItemBestBefore.setInputType(InputType.TYPE_NULL);
        String bestBeforeDateFormatted =
                addItemViewModel.DATE_FORMATTER.print(addItemViewModel.getItem().getBestBeforeDate());
        mBinding.etAddItemBestBefore.setText(bestBeforeDateFormatted);

        // Set up the freezing date picker
        freezingDatePicker = createDatePicker(
                R.string.add_item_frozen_at_date_picker_title_text,
                addItemViewModel.getItem().getFrozenAtDate() != null ?
                        addItemViewModel.getItem().getFrozenAtDate() : currentDate
        );

        // Add the behavior for the positive button of the freezing date picker
        freezingDatePicker.addOnPositiveButtonClickListener(selection -> {
            LocalDate date = convertSelectedDate(selection);
            addItemViewModel.getItem().setFrozenAtDate(date);
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

        // Set up the best before date picker
        MaterialDatePicker<Long> bestBeforeDatePicker = createDatePicker(
                R.string.add_item_best_before_date_picker_title_text,
                addItemViewModel.getItem().getBestBeforeDate()
        );

        bestBeforeDatePicker.addOnPositiveButtonClickListener(selection -> {
            LocalDate date = convertSelectedDate(selection);
            addItemViewModel.getItem().setBestBeforeDate(date);
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

    private LocalDate convertSelectedDate(final Long selection) {
        return LocalDate.fromDateFields(new Date(selection));
    }

    /**
     * Sets up the Floating Action Button for saving the new item
     */
    private void setUpFloatingActionButton() {
        // TODO Set up for saving an existing entry

        mBinding.floatingActionButton.setOnClickListener(fab -> {
            // TODO Check validity of inputs
            Log.d("AddItem", "Clicked on Save button");

            // Input check: Name may not be empty
            if (addItemViewModel.getItem().getName().isEmpty()) {
                mBinding.addItemNameLayout.setErrorEnabled(true);
                mBinding.addItemNameLayout.setError("Name may not be empty");
                return;
            }

            addItemViewModel.save();

            Keyboard.hideKeyboardFrom(getActivity(), fab);
            Navigation.findNavController(fab).navigate(R.id.action_new_item_save);
        });
    }


    /**
     * The Adapter for the list of pending or created notifications.
     */
    private class NotificationListAdapter
            extends RecyclerView.Adapter<NotificationListAdapter.NotificationListAdapterViewHolder> {

        private List<ItemNotification> notifications;

        public NotificationListAdapter() {
            this.notifications = new ArrayList<>();
        }

        private void setItems(List<ItemNotification> notificationList) {
            this.notifications = new ArrayList<>(notificationList);
            notifyDataSetChanged();
        }


        private void addNotificationEntry(ItemNotification notification) {
            Log.d(TAG, "Adding notification entry to RV");
            notifications.add(notification);
            notifyItemInserted(getItemCount());
        }


        private void removeNotificationEntry(final ItemNotification notification) {
            Log.d(TAG, "Removing notification entry from RV");
            int positionOfNotification = getPositionOfNotification(notification);
            notifications.remove(notification);
            notifyItemRemoved(positionOfNotification);
        }


        @NonNull
        @Override
        public NotificationListAdapterViewHolder onCreateViewHolder(
                @NonNull final ViewGroup parent,
                final int viewType) {

            View view = getLayoutInflater().inflate(R.layout.notification_entry, parent, false);
            return new NotificationListAdapterViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final NotificationListAdapterViewHolder holder, final int position) {
            ItemNotification notification = getNotificationAtPosition(position);
            TimeOffsetUnit timeOffsetUnit = notification.getTimeOffsetUnit();
            int offsetAmount = notification.getOffsetAmount();

            switch (timeOffsetUnit) {
                case DAYS:
                    holder.entry.setText(getResources().getQuantityString(
                            R.plurals.notification_list_entry_days_capitalized,
                            offsetAmount,
                            offsetAmount
                    ));
                    break;
                case WEEKS:
                    holder.entry.setText(getResources().getQuantityString(
                            R.plurals.notification_list_entry_weeks_capitalized,
                            offsetAmount,
                            offsetAmount
                    ));
                    break;
                case MONTHS:
                    holder.entry.setText(getResources().getQuantityString(
                            R.plurals.notification_list_entry_months_capitalized,
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
                    if (event.getRawX() >= (holder.entry.getRight()
                            - holder.entry.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        // Remove notification from list
                        addItemViewModel.addNotificationToDelete(notification);
                        mNotificationListAdapter.removeNotificationEntry(notification);
                        return true;
                    }
                }
                return false;
            });
        }


        private int getPositionOfNotification(final ItemNotification notification) {
            return notifications.indexOf(notification);
        }

        private ItemNotification getNotificationAtPosition(int position) {
            return notifications.get(position);
        }

        @Override
        public int getItemCount() {
            return notifications.size();
        }

        /**
         * The view holder for the notification list recycler view
         */
        private class NotificationListAdapterViewHolder extends RecyclerView.ViewHolder {

            private TextView entry;

            public NotificationListAdapterViewHolder(@NonNull final View itemView) {
                super(itemView);

                entry = (TextView) itemView;
            }
        }
    }
}
