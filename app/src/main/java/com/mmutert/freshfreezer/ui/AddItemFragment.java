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
import com.mmutert.freshfreezer.R;
import com.mmutert.freshfreezer.data.AmountUnit;
import com.mmutert.freshfreezer.data.FrozenItem;
import com.mmutert.freshfreezer.databinding.FragmentAddItemBinding;
import com.mmutert.freshfreezer.util.Keyboard;
import com.mmutert.freshfreezer.viewmodel.AddItemViewModel;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.Date;

import static com.mmutert.freshfreezer.viewmodel.FrozenItemViewModel.DATE_FORMATTER;


public class AddItemFragment extends Fragment {

    public static final String TAG = AddItemFragment.class.getName();

    private AddItemViewModel addItemViewModel;

    private FragmentAddItemBinding mBinding;
    private MaterialDatePicker<Long> freezingDatePicker;
    private UnitArrayAdapter spinnerUnitAdapter;

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
                addItemViewModel.newItem();
            } else {
                addItemViewModel
                        .getItemAndNotifications(itemId)
                        .observe(getViewLifecycleOwner(), itemAndNotifications1 -> {
                            addItemViewModel.setCurrentItem(itemAndNotifications1.item);
                            updateWithNewData();
                        });
            }
        }

        mBinding.setCurrentItem(addItemViewModel.getItem());

        setUpFloatingActionButton();
        setUpDatePickers();
        setUpFreezingDateButton();
        setUpAddNotificationButton();
        setUpUnitSpinner();

        updateWithNewData();
    }

    private void updateWithNewData() {
        FrozenItem item = addItemViewModel.getItem();

        mBinding.setCurrentItem(item);

        // Unit spinner
        mBinding.spAddItemsUnitSelection.setSelection(spinnerUnitAdapter.getIndexOfUnit(item.getUnit()));

        // TODO Notifications

        LocalDate frozenAtDate = item.getFrozenAtDate();
        if (frozenAtDate != null) {
            showFreezingDate();

            String selectedFrozenDateFormatted = DATE_FORMATTER.print(frozenAtDate);
            mBinding.etAddItemFrozenDate.setText(selectedFrozenDateFormatted);
        } else {
            showAddFreezingDateButton();
        }

        LocalDate bestBeforeDate = item.getBestBeforeDate();
        String bestBeforeDateFormatted = DATE_FORMATTER.print(bestBeforeDate);
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

            PendingNotification notification = new PendingNotification();

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
                        addItemViewModel.removePendingNotification(notification);

                        // Remove UI element
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
                addItemViewModel.addPendingNotification(notification);

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
        addItemViewModel.getItem().setBestBeforeDate(currentDate);

        String curDateFormatted = DATE_FORMATTER.print(currentDate);

        mBinding.etAddItemFrozenDate.setInputType(InputType.TYPE_NULL);
        mBinding.etAddItemFrozenDate.setText(curDateFormatted);

        mBinding.etAddItemBestBefore.setInputType(InputType.TYPE_NULL);
        mBinding.etAddItemBestBefore.setText(curDateFormatted);

        // Set up the freezing date picker
        freezingDatePicker = createDatePicker(
                R.string.add_item_frozen_at_date_picker_title_text,
                addItemViewModel.getItem().getFrozenAtDate() != null ?
                        addItemViewModel.getItem().getFrozenAtDate() : currentDate
        );
        freezingDatePicker.addOnPositiveButtonClickListener(selection -> {
            LocalDate date = convertSelectedDate(selection);
            addItemViewModel.getItem().setFrozenAtDate(date);
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
                    addItemViewModel.getItem().getBestBeforeDate()
            );

            picker.addOnPositiveButtonClickListener(selection -> {
                LocalDate date = convertSelectedDate(selection);
                addItemViewModel.getItem().setBestBeforeDate(date);
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

            addItemViewModel.insertItem();
            addItemViewModel.scheduleNotifications();

            Keyboard.hideKeyboardFrom(getActivity(), fab);
            Navigation.findNavController(fab).navigate(R.id.action_new_item_save);
        });
    }

}
