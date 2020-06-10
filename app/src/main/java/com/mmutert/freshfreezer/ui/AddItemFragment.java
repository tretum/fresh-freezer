package com.mmutert.freshfreezer.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.text.InputType;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mmutert.freshfreezer.R;
import com.mmutert.freshfreezer.data.FrozenItem;
import com.mmutert.freshfreezer.viewmodel.FrozenItemViewModel;
import com.mmutert.freshfreezer.databinding.FragmentAddItemBinding;

import java.util.Calendar;
import java.util.Date;


public class AddItemFragment extends Fragment {

    private FrozenItemViewModel frozenItemViewModel;


    private FrozenItem newItem;
    private FragmentAddItemBinding mBinding;

    public AddItemFragment() {
        // Required empty public constructor
        newItem = new FrozenItem();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBinding = FragmentAddItemBinding.inflate(inflater, container, false);

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpButtons();
        setupDatePickers();

        this.frozenItemViewModel = new ViewModelProvider(requireActivity()).get(FrozenItemViewModel.class);
    }

    private void setupDatePickers() {
        // Set up the date pickers for the best before and frozen date fields
        final Calendar c = Calendar.getInstance();
        int currentYear = c.get(Calendar.YEAR);
        int currentMonth = c.get(Calendar.MONTH);
        int currentDay = c.get(Calendar.DAY_OF_MONTH);
        String curDateFormatted = DateFormat.format("yyyy-MM-dd", c.getTime()).toString();

        newItem.setFrozenDate(c.getTime());
        newItem.setBestBeforeDate(c.getTime());

        mBinding.etAddItemFrozenDate.setInputType(InputType.TYPE_NULL);
        mBinding.etAddItemFrozenDate.setText(curDateFormatted);

        mBinding.etAddItemBestBefore.setInputType(InputType.TYPE_NULL);
        mBinding.etAddItemBestBefore.setText(curDateFormatted);

        mBinding.etAddItemFrozenDate.setOnClickListener(v -> {
            new DatePickerDialog(requireActivity(), (view, year, month, dayOfMonth) -> {
                Calendar c2 = Calendar.getInstance();
                c2.set(year, month, dayOfMonth, 0, 0);
                Date selectedFrozenDate = c2.getTime();
                newItem.setFrozenDate(selectedFrozenDate);

                String selectedFrozenDateFormatted =
                        DateFormat.format("yyyy-MM-dd", selectedFrozenDate).toString();
                mBinding.etAddItemFrozenDate.setText(selectedFrozenDateFormatted);
            }, currentYear, currentMonth, currentDay).show();
        });

        mBinding.etAddItemBestBefore.setOnClickListener(v -> {
            new DatePickerDialog(requireActivity(), (view, year, month, dayOfMonth) -> {
                Calendar c2 = Calendar.getInstance();
                c2.set(year, month, dayOfMonth, 0, 0);
                Date selectedBestBeforeDate = c2.getTime();
                newItem.setBestBeforeDate(selectedBestBeforeDate);

                String selectedBestBeforeFormatted =
                        DateFormat.format("yyyy-MM-dd", selectedBestBeforeDate).toString();
                mBinding.etAddItemBestBefore.setText(selectedBestBeforeFormatted);
            }, currentYear, currentMonth, currentDay).show();
        });
    }

    private void setUpButtons(){
        mBinding.floatingActionButton.setOnClickListener(v -> {
            // TODO Check validity of inputs
            // TODO Use the data binding newItem variable
            Log.d("AddItem", "Clicked on Save button");

            newItem.setName(mBinding.etAddItemTitle.getText().toString());
            newItem.setId(0);

            frozenItemViewModel.insert(newItem);

            Navigation.findNavController(v).navigate(R.id.action_new_item_save);
        });
    }

}
