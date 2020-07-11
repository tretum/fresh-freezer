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
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;
import com.mmutert.freshfreezer.R;
import com.mmutert.freshfreezer.data.AmountUnit;
import com.mmutert.freshfreezer.data.FrozenItem;
import com.mmutert.freshfreezer.databinding.FragmentAddItemBinding;
import com.mmutert.freshfreezer.notification.NotificationHelper;
import com.mmutert.freshfreezer.util.Keyboard;
import com.mmutert.freshfreezer.viewmodel.FrozenItemViewModel;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import static com.mmutert.freshfreezer.notification.NotificationConstants.NOTIFICATION_OFFSET_TIMEUNIT;
import static com.mmutert.freshfreezer.viewmodel.FrozenItemViewModel.DATE_FORMATTER;


public class AddItemFragment extends Fragment {

    public static final String TAG = AddItemFragment.class.getName();
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    private FrozenItemViewModel frozenItemViewModel;


    private FrozenItem newItem;
    private FragmentAddItemBinding mBinding;
    private List<PendingNotification> notifications = new ArrayList<>();

    private Snackbar mSnackbar;
    private MaterialDatePicker<Long> freezingDatePicker;

    public AddItemFragment() {
        // Required empty public constructor
        newItem = new FrozenItem();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBinding = FragmentAddItemBinding.inflate(inflater, container, false);
        mBinding.setNewItem(newItem);

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpFloatingActionButton();
        setupDatePickers();
        setupFreezingDateButton();
        setUpAddNotificationButton();

        UnitArrayAdapter unitAdapter = new UnitArrayAdapter(
                getContext(),
                android.R.layout.simple_spinner_dropdown_item
        );
        mBinding.spAddItemsUnitSelection.setAdapter(unitAdapter);
//        mBinding.spAddItemsUnitSelection.setAdapter(new ArrayAdapter<>(
//                getContext(),
//                android.R.layout.simple_spinner_dropdown_item,
//                AmountUnit.values()
//        ));
        mBinding.spAddItemsUnitSelection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                AmountUnit selectedUnit = unitAdapter.getSelectedUnit(position);
                newItem.setUnit(selectedUnit);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                newItem.setUnit(AmountUnit.GRAMS);
            }
        });

        this.frozenItemViewModel = new ViewModelProvider(requireActivity()).get(FrozenItemViewModel.class);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setUpAddNotificationButton() {
        TextView tvAddNotification = mBinding.tvAddNotification;
        tvAddNotification.setOnClickListener(v -> {

            // Add pending notification to local list
            // TODO Set values to selection
            // TODO Add dialogs that allow selecting a notification offset or actual time
            PendingNotification notification = new PendingNotification();

            // Add the entry to the notifications list
            TextView notificationTextView = (TextView) getLayoutInflater().inflate(
                    R.layout.notification_entry,
                    mBinding.addItemNotificationLayout,
                    false
            );

            int id = ViewCompat.generateViewId();
            notificationTextView.setId(id);

            mBinding.addItemNotificationLayout.addView(
                    notificationTextView,
                    mBinding.addItemNotificationLayout.getChildCount() - 1
            );

            // Add the delete button for the notification in the list
            notificationTextView.setOnTouchListener((v1, event) -> {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (event.getRawX() >= (notificationTextView.getRight()
                            - notificationTextView.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        // Remove notification from list
                        notifications.remove(notification);
                        mBinding.addItemNotificationLayout.removeView(notificationTextView);
                        return true;
                    }
                }
                return false;
            });


            // Open notification dialog
            new NotificationOffsetDialogFragment(dialog -> {
                Log.d(TAG, "Selected notification offset from dialog.");

                PendingNotification.OffsetUnit offSetUnit = dialog.getOffSetAmount();
                int enteredOffset = dialog.getEnteredOffset();

                Log.d(TAG, "Selected Offset: " + enteredOffset);
                Log.d(TAG, "Selected Unit: " + offSetUnit);

                notification.setOffsetAmount(enteredOffset);
                notification.setTimeUnit(offSetUnit);
                notifications.add(notification);

                switch (offSetUnit) {
                    case DAYS:
                        notificationTextView.setText(getResources().getQuantityString(
                                R.plurals.notification_list_entry_days_capitalized,
                                enteredOffset,
                                enteredOffset
                        ));
                        break;
                    case WEEKS:
                        notificationTextView.setText(getResources().getQuantityString(
                                R.plurals.notification_list_entry_weeks_capitalized,
                                enteredOffset,
                                enteredOffset
                        ));
                        break;
                    case MONTHS:
                        notificationTextView.setText(getResources().getQuantityString(
                                R.plurals.notification_list_entry_months_capitalized,
                                enteredOffset,
                                enteredOffset
                        ));
                        break;
                }
            }).show(getParentFragmentManager(), "add notification");
        });
    }

    private void setupFreezingDateButton() {
        mBinding.tvAddFreezingDate.setOnClickListener(v -> {
            mBinding.tvAddFreezingDate.setVisibility(View.GONE);
            mBinding.rlFreezingDateLayout.setVisibility(View.VISIBLE);
            freezingDatePicker.show(getParentFragmentManager(), freezingDatePicker.toString());
        });
    }

    private MaterialDatePicker<Long> createDatePicker(int stringId, LocalDate date) {
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText(getResources().getString(stringId));
        // TODO Fix hack with adding hours to get the correct selection
        builder.setSelection(date.toLocalDateTime(LocalTime.MIDNIGHT.plusHours(12)).toDate().getTime());
        return builder.build();
    }

    /**
     * Sets up the date picker dialogs for the date of freezing the item and the best before date.
     */
    private void setupDatePickers() {
        // Set up the date pickers for the best before and frozen date fields

        LocalDate currentDate = LocalDate.now();
        newItem.setBestBeforeDate(currentDate);

        String curDateFormatted = DATE_FORMATTER.print(currentDate);

        mBinding.etAddItemFrozenDate.setInputType(InputType.TYPE_NULL);
        mBinding.etAddItemFrozenDate.setText(curDateFormatted);

        mBinding.etAddItemBestBefore.setInputType(InputType.TYPE_NULL);
        mBinding.etAddItemBestBefore.setText(curDateFormatted);

        // Set up the freezing date picker
        freezingDatePicker = createDatePicker(
                R.string.add_item_frozen_at_date_picker_title_text,
                newItem.getFrozenAtDate() != null ? newItem.getFrozenAtDate() : currentDate
        );
        freezingDatePicker.addOnPositiveButtonClickListener(selection -> {
            LocalDate date = convertSelectedDate(selection);
            newItem.setFrozenAtDate(date);
            String selectedFrozenDateFormatted = DATE_FORMATTER.print(date);
            mBinding.etAddItemFrozenDate.setText(selectedFrozenDateFormatted);
        });

        mBinding.etAddItemFrozenDate.setOnClickListener(v -> {
            freezingDatePicker.show(getParentFragmentManager(), freezingDatePicker.toString());
        });

        // Set up the best before date picker
        mBinding.etAddItemBestBefore.setOnClickListener(v -> {
            MaterialDatePicker<Long> picker = createDatePicker(
                    R.string.add_item_best_before_date_picker_title_text,
                    newItem.getBestBeforeDate()
            );

            picker.addOnPositiveButtonClickListener(selection -> {
                LocalDate date = convertSelectedDate(selection);
                newItem.setBestBeforeDate(date);
                String selectedFrozenDateFormatted = DATE_FORMATTER.print(date);
                mBinding.etAddItemBestBefore.setText(selectedFrozenDateFormatted);
            });

            picker.show(getParentFragmentManager(), picker.toString());
        });
    }

    private LocalDate convertSelectedDate(final Long selection) {
        return LocalDate.fromDateFields(new Date(selection));
    }

    /**
     * Adds remaining item properties and inserts the items into the database using the view model.
     */
    private void insertItem() {
        newItem.setId(0);
        newItem.setItemCreationDate(LocalDateTime.now());
        newItem.setLastChangedAtDate(LocalDateTime.now());
        frozenItemViewModel.insert(newItem);
    }

    /**
     * Sets up the Floating Action Button for saving the new item
     */
    private void setUpFloatingActionButton() {
        mBinding.floatingActionButton.setOnClickListener(v -> {
            // TODO Check validity of inputs
            Log.d("AddItem", "Clicked on Save button");


            if (newItem.getName().isEmpty()) {
                if (this.mSnackbar != null) {
                    this.mSnackbar.dismiss();
                    this.mSnackbar = null;
                }
                this.mSnackbar = Snackbar.make(
                        v,
                        getResources().getString(R.string.add_item_saving_failed_text),
                        Snackbar.LENGTH_SHORT
                );
                this.mSnackbar.show();
            } else {
                insertItem();

                Navigation.findNavController(v).navigate(R.id.action_new_item_save);

                // TODO Check for possible race conditions where id for the item might not be set yet
                for (PendingNotification notification : notifications) {
                    Log.d(TAG, "Scheduling notification");

                    LocalTime notificationTime = LocalTime.now(DateTimeZone.forTimeZone(TimeZone.getDefault()));
                    // TODO Set notification time to the one saved in the pending notification object, otherwise used preference

                    LocalDateTime scheduledOn = newItem
                            .getBestBeforeDate()
                            .toLocalDateTime(notificationTime);

                    switch (notification.getTimeUnit()) {
                        case DAYS:
                            scheduledOn = scheduledOn.minusDays(notification.getOffsetAmount());
                            break;
                        case WEEKS:
                            scheduledOn = scheduledOn.minusWeeks(notification.getOffsetAmount());
                            break;
                        case MONTHS:
                            scheduledOn = scheduledOn.minusMonths(notification.getOffsetAmount());
                            break;
                    }

                    if (!LocalDateTime.now().isBefore(scheduledOn)) {
                        UUID uuid = NotificationHelper.scheduleNotification(
                                getContext(),
                                newItem,
                                NOTIFICATION_OFFSET_TIMEUNIT,
                                scheduledOn
                        );
                        frozenItemViewModel.addNotification(newItem, uuid, scheduledOn);
                    }
                }

                Keyboard.hideKeyboardFrom(getContext(), v);
            }
        });
    }

}
