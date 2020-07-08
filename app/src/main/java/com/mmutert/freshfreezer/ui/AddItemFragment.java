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
import android.widget.ArrayAdapter;
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

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import static com.mmutert.freshfreezer.notification.NotificationConstants.NOTIFICATION_OFFSET_TIMEUNIT;


public class AddItemFragment extends Fragment {

    public static final String TAG = AddItemFragment.class.getName();
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    private FrozenItemViewModel frozenItemViewModel;


    private FrozenItem newItem;
    private FragmentAddItemBinding mBinding;
    private List<PendingNotification> notifications = new ArrayList<>();

    private Snackbar mSnackbar;

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
        setUpAddNotificationButton();

        mBinding.spAddItemsUnitSelection.setAdapter(new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                AmountUnit.values()
        ));
        mBinding.spAddItemsUnitSelection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Object itemAtPosition = parent.getItemAtPosition(position);
                if (itemAtPosition instanceof AmountUnit) {
                    AmountUnit atPosition = (AmountUnit) itemAtPosition;
                    newItem.setUnit(atPosition);
                } else {
                    Log.d(TAG, "Selected item is not a AmountUnit");
                }
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

                notificationTextView.setText(String.format(
                        Locale.getDefault(),
                        "%d %s before",
                        enteredOffset,
                        offSetUnit.toString()
                ));
            }).show(getParentFragmentManager(), "add notification");
        });
    }

    /**
     * Sets up the date picker dialogs for the date of freezing the item and the best before date.
     */
    private void setupDatePickers() {
        // Set up the date pickers for the best before and frozen date fields

        LocalDate currentDate = LocalDate.now();
        newItem.setFrozenAtDate(currentDate);
        newItem.setBestBeforeDate(currentDate);
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATE_FORMAT);
        String curDateFormatted = dateTimeFormatter.print(currentDate);

        mBinding.etAddItemFrozenDate.setInputType(InputType.TYPE_NULL);
        mBinding.etAddItemFrozenDate.setText(curDateFormatted);

        mBinding.etAddItemBestBefore.setInputType(InputType.TYPE_NULL);
        mBinding.etAddItemBestBefore.setText(curDateFormatted);

        mBinding.etAddItemFrozenDate.setOnClickListener(v -> {
            MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
            builder.setTitleText(getResources().getString(R.string.add_item_frozen_at_date_picker_title_text));
            builder.setSelection(newItem.getFrozenAtDate().toDate().getTime());
            MaterialDatePicker<Long> picker = builder.build();

            picker.addOnPositiveButtonClickListener(selection -> {
                LocalDate date = LocalDate.fromDateFields(new Date(selection));
                newItem.setFrozenAtDate(date);
                String selectedFrozenDateFormatted = dateTimeFormatter.print(date);
                mBinding.etAddItemFrozenDate.setText(selectedFrozenDateFormatted);
            });

            picker.show(getParentFragmentManager(), picker.toString());
        });

        mBinding.etAddItemBestBefore.setOnClickListener(v -> {
            MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
            builder.setTitleText(getResources().getString(R.string.add_item_best_before_date_picker_title_text));
            builder.setSelection(newItem.getFrozenAtDate().toDate().getTime());
            MaterialDatePicker<Long> picker = builder.build();

            picker.addOnPositiveButtonClickListener(selection -> {
                LocalDate date = LocalDate.fromDateFields(new Date(selection));
                newItem.setBestBeforeDate(date);
                String selectedFrozenDateFormatted = dateTimeFormatter.print(date);
                mBinding.etAddItemBestBefore.setText(selectedFrozenDateFormatted);
            });

            picker.show(getParentFragmentManager(), picker.toString());
        });
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
