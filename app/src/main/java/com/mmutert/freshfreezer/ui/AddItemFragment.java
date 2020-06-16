package com.mmutert.freshfreezer.ui;

import android.app.DatePickerDialog;
import android.app.NotificationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.text.InputType;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.mmutert.freshfreezer.MainActivity;
import com.mmutert.freshfreezer.R;
import com.mmutert.freshfreezer.data.AmountUnit;
import com.mmutert.freshfreezer.data.FrozenItem;
import com.mmutert.freshfreezer.notification.NotificationConstants;
import com.mmutert.freshfreezer.notification.NotificationWorker;
import com.mmutert.freshfreezer.util.Keyboard;
import com.mmutert.freshfreezer.viewmodel.FrozenItemViewModel;
import com.mmutert.freshfreezer.databinding.FragmentAddItemBinding;

import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;


public class AddItemFragment extends Fragment {

    private FrozenItemViewModel frozenItemViewModel;


    private FrozenItem newItem;
    private FragmentAddItemBinding mBinding;

    private Toast mToast;

    public AddItemFragment() {
        // Required empty public constructor
        newItem = new FrozenItem();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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

        setUpButtons();
        setupDatePickers();

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
                    Log.d("Selected item", "Selected item is not a AmountUnit");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                newItem.setUnit(AmountUnit.GRAMS);
            }
        });

        this.frozenItemViewModel = new ViewModelProvider(requireActivity()).get(FrozenItemViewModel.class);
    }

    private void setupDatePickers() {
        // Set up the date pickers for the best before and frozen date fields
        Date currentTime = Calendar.getInstance().getTime();
        String curDateFormatted = DateFormat.format("yyyy-MM-dd", currentTime).toString();

        newItem.setFrozenDate(currentTime);
        newItem.setBestBeforeDate(currentTime);

        mBinding.etAddItemFrozenDate.setInputType(InputType.TYPE_NULL);
        mBinding.etAddItemFrozenDate.setText(curDateFormatted);

        mBinding.etAddItemBestBefore.setInputType(InputType.TYPE_NULL);
        mBinding.etAddItemBestBefore.setText(curDateFormatted);

        mBinding.etAddItemFrozenDate.setOnClickListener(v -> {
            MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
            builder.setTitleText("Select Freeze Date");
            builder.setSelection(newItem.getFrozenDate().getTime());
            MaterialDatePicker<Long> picker = builder.build();

            picker.addOnPositiveButtonClickListener(selection -> {
                Date date = new Date(selection);
                newItem.setFrozenDate(date);
                String selectedFrozenDateFormatted = DateFormat.format("yyyy-MM-dd", date).toString();
                mBinding.etAddItemFrozenDate.setText(selectedFrozenDateFormatted);
            });

            picker.show(getParentFragmentManager(), picker.toString());
        });

        mBinding.etAddItemBestBefore.setOnClickListener(v -> {
            MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
            builder.setTitleText("Best Before Date");
            builder.setSelection(newItem.getBestBeforeDate().getTime());
            MaterialDatePicker<Long> picker = builder.build();

            picker.addOnPositiveButtonClickListener(selection -> {
                Date date = new Date(selection);
                newItem.setBestBeforeDate(date);
                String selectedFrozenDateFormatted = DateFormat.format("yyyy-MM-dd", date).toString();
                mBinding.etAddItemBestBefore.setText(selectedFrozenDateFormatted);
            });

            picker.show(getParentFragmentManager(), picker.toString());
        });
    }

    private void setUpButtons() {
        mBinding.floatingActionButton.setOnClickListener(v -> {
            // TODO Check validity of inputs
            Log.d("AddItem", "Clicked on Save button");

            newItem.setId(0);

            if (mBinding.getNewItem().getName() == null || mBinding.getNewItem().getName().isEmpty()) {
                if (this.mToast != null) {
                    this.mToast.cancel();
                    this.mToast = null;
                }
                this.mToast = Toast.makeText(
                        getContext(),
                        "Saving failed. A new entry requires a name!",
                        Toast.LENGTH_SHORT
                );
                this.mToast.show();
            } else {
                frozenItemViewModel.insert(newItem);
                Navigation.findNavController(v).navigate(R.id.action_new_item_save);

                // TODO Schedule notification
                String notificationUUID = scheduleNotification(newItem);
                // TODO Save notification to database


                Keyboard.hideKeyboardFrom(getContext(), v);
            }
        });
    }

    private String scheduleNotification(FrozenItem item) {
        // TODO Set correct values
//        TimeUnit notificationOffsetUnit = TimeUnit.DAYS;
//        long notificationOffset = calculateOffset(item);
        TimeUnit notificationOffsetUnit = TimeUnit.SECONDS;
        long notificationOffset = 10;

        OneTimeWorkRequest notificationRequest =
                new OneTimeWorkRequest.Builder(NotificationWorker.class)
                        .setInputData(createInputDataForItem(item))
                        .setConstraints(
                                new Constraints.Builder()
                                        .setRequiresBatteryNotLow(true)
                                        .build())
                        .setInitialDelay(
                                notificationOffset,
                                notificationOffsetUnit
                        )
                        .build();

        WorkManager.getInstance(getActivity()).enqueue(notificationRequest);
        return notificationRequest.getId().toString();
    }

    private long calculateOffset(FrozenItem item) {
        long notificationTimeOffset = TimeUnit.MILLISECONDS.convert(7, TimeUnit.DAYS);

        Date bestBeforeDate = item.getBestBeforeDate();
        long bestBeforeInMillis = bestBeforeDate.getTime();
        long currentTimeInMillis = Calendar.getInstance().getTimeInMillis();
        return TimeUnit.DAYS.convert(
                Math.abs((bestBeforeInMillis - notificationTimeOffset) - currentTimeInMillis),
                TimeUnit.MILLISECONDS
        );
    }

    private Data createInputDataForItem(FrozenItem item) {
        return new Data.Builder()
                .putString(NotificationConstants.KEY_ITEM_NAME, item.getName())
                .putFloat(NotificationConstants.KEY_ITEM_AMOUNT, item.getAmount())
                .putInt(NotificationConstants.KEY_ITEM_ID, (int) item.getId())
                .putString(NotificationConstants.KEY_ITEM_AMOUNT_UNIT, item.getUnit().toString())
                .build();
    }

}
