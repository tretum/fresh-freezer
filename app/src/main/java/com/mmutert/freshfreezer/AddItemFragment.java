package com.mmutert.freshfreezer;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.mmutert.freshfreezer.data.FrozenItem;
import com.mmutert.freshfreezer.data.FrozenItemViewModel;
import com.mmutert.freshfreezer.databinding.FragmentAddItemBinding;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class AddItemFragment extends Fragment {

    private FrozenItemViewModel frozenItemViewModel;


    private FrozenItem newItem;
    private FragmentAddItemBinding mBinding;

    public AddItemFragment() {
        // Required empty public constructor
        newItem = new FrozenItem();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBinding = FragmentAddItemBinding.inflate(inflater, container, false);

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
        mBinding.etAddItemFrozenDate.setInputType(InputType.TYPE_NULL);
        mBinding.etAddItemFrozenDate.setText(String.format(Locale.getDefault(), "%d-%d-%d", currentYear, currentMonth + 1, currentDay));
        mBinding.etAddItemBestBefore.setInputType(InputType.TYPE_NULL);
        mBinding.etAddItemBestBefore.setText(String.format(Locale.getDefault(), "%d-%d-%d", currentYear, currentMonth + 1, currentDay));

        mBinding.etAddItemFrozenDate.setOnClickListener(v -> {
            new DatePickerDialog(requireActivity(), (DatePickerDialog.OnDateSetListener) (view, year, month, dayOfMonth) -> {
                Calendar c2 = Calendar.getInstance();
                c2.set(year, month + 1, dayOfMonth, 0, 0);
                Date selectedFrozenDate = c2.getTime();
                newItem.setFrozenDate(selectedFrozenDate);
                mBinding.etAddItemFrozenDate.setText(String.format(Locale.getDefault(), "%d-%d-%d", year, (month + 1), dayOfMonth));
            }, currentYear, currentMonth, currentDay).show();
        });

        mBinding.etAddItemBestBefore.setOnClickListener(v -> {
            new DatePickerDialog(requireActivity(), (DatePickerDialog.OnDateSetListener) (view, year, month, dayOfMonth) -> {
                Calendar c2 = Calendar.getInstance();
                c2.set(year, month + 1, dayOfMonth, 0, 0);
                Date selectedBestBeforeDate = c2.getTime();
                newItem.setBestBeforeDate(selectedBestBeforeDate);
                mBinding.etAddItemBestBefore.setText(String.format(Locale.getDefault(), "%d-%d-%d", year, (month + 1), dayOfMonth));
            }, currentYear, currentMonth, currentDay).show();
        });
    }

    private void setupConfirmButton(){
        mBinding.btAddItemConfirm.setOnClickListener(v -> {
            FrozenItem frozenItem = new FrozenItem();
            frozenItem.setName(mBinding.etAddItemTitle.getText().toString());
            frozenItem.setDescription(mBinding.etAddItemDescription.getText().toString());
            // TODO Finish
        });

    }

    public static AddItemFragment createAddItemFragment() {
        AddItemFragment addItemFragment = new AddItemFragment();

        return addItemFragment;
    }
}
