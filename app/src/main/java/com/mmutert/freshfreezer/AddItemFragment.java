package com.mmutert.freshfreezer;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.mmutert.freshfreezer.data.FrozenItem;
import com.mmutert.freshfreezer.data.FrozenItemViewModel;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class AddItemFragment extends Fragment {

    private FrozenItemViewModel frozenItemViewModel;

    private Button mSubmitButton;
    private Button mDiscardButton;

    private EditText mTitleEditText;
    private EditText mDescriptionEditText;
    private EditText mFrozenDateEditText;
    private EditText mBestBeforeDateEditText;
    private EditText mAmountEditText;

    private FrozenItem newItem;

    public AddItemFragment() {
        // Required empty public constructor
        newItem = new FrozenItem();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_item, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.mSubmitButton = view.findViewById(R.id.bt_add_item_confirm);
        this.mDiscardButton = view.findViewById(R.id.bt_add_item_discard);

        this.mTitleEditText = view.findViewById(R.id.et_add_item_title);
        this.mDescriptionEditText = view.findViewById(R.id.et_add_item_description);
        this.mFrozenDateEditText = view.findViewById(R.id.et_add_item_frozen_date);
        this.mBestBeforeDateEditText = view.findViewById(R.id.et_add_item_best_before);
        this.mAmountEditText = view.findViewById(R.id.et_add_item_amount);

        setupConfirmButton();
        setupDatePickers();

        this.frozenItemViewModel = new ViewModelProvider(requireActivity()).get(FrozenItemViewModel.class);
    }

    private void setupDatePickers() {
        // Set up the date pickers for the best before and frozen date fields
        final Calendar c = Calendar.getInstance();
        int currentYear = c.get(Calendar.YEAR);
        int currentMonth = c.get(Calendar.MONTH);
        int currentDay = c.get(Calendar.DAY_OF_MONTH);
        mFrozenDateEditText.setInputType(InputType.TYPE_NULL);
        mFrozenDateEditText.setText(String.format(Locale.getDefault(), "%d-%d-%d", currentYear, currentMonth + 1, currentDay));
        mBestBeforeDateEditText.setInputType(InputType.TYPE_NULL);
        mBestBeforeDateEditText.setText(String.format(Locale.getDefault(), "%d-%d-%d", currentYear, currentMonth + 1, currentDay));

        this.mFrozenDateEditText.setOnClickListener(v -> {
            new DatePickerDialog(requireActivity(), (DatePickerDialog.OnDateSetListener) (view, year, month, dayOfMonth) -> {
                Calendar c2 = Calendar.getInstance();
                c2.set(year, month + 1, dayOfMonth, 0, 0);
                Date selectedFrozenDate = c2.getTime();
                newItem.setFrozenDate(selectedFrozenDate);
                mFrozenDateEditText.setText(String.format(Locale.getDefault(), "%d-%d-%d", year, (month + 1), dayOfMonth));
            }, currentYear, currentMonth, currentDay).show();
        });

        this.mBestBeforeDateEditText.setOnClickListener(v -> {
            new DatePickerDialog(requireActivity(), (DatePickerDialog.OnDateSetListener) (view, year, month, dayOfMonth) -> {
                Calendar c2 = Calendar.getInstance();
                c2.set(year, month + 1, dayOfMonth, 0, 0);
                Date selectedBestBeforeDate = c2.getTime();
                newItem.setBestBeforeDate(selectedBestBeforeDate);
                mBestBeforeDateEditText.setText(String.format(Locale.getDefault(), "%d-%d-%d", year, (month + 1), dayOfMonth));
            }, currentYear, currentMonth, currentDay).show();
        });
    }

    private void setupConfirmButton(){
        this.mSubmitButton.setOnClickListener(v -> {
            FrozenItem frozenItem = new FrozenItem();
            frozenItem.setName(mTitleEditText.getText().toString());
            frozenItem.setDescription(mDescriptionEditText.getText().toString());
            // TODO Finish
        });

    }

    public static AddItemFragment createAddItemFragment() {
        AddItemFragment addItemFragment = new AddItemFragment();

        return addItemFragment;
    }
}
