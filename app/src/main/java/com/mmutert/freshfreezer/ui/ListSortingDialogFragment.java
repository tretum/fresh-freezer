package com.mmutert.freshfreezer.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import com.mmutert.freshfreezer.R;
import com.mmutert.freshfreezer.databinding.FilterActionOptionsBinding;
import com.mmutert.freshfreezer.util.SortingOption;


public class ListSortingDialogFragment extends DialogFragment {

    private Context context;
    private ListSortingChangedListener listener;
    private SortingOption.SortingOrder currentOrder;
    private SortingOption currentOption;


    public interface ListSortingChangedListener {
        void listOptionClicked(SortingOption selectedSortingOption, SortingOption.SortingOrder sortingOrder);
    }

    public ListSortingDialogFragment(
            Context context,
            SortingOption option,
            SortingOption.SortingOrder order,
            ListSortingChangedListener listener) {
        this.context       = context;
        this.currentOption = option;
        this.currentOrder  = order;
        this.listener      = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable final Bundle savedInstanceState) {

        FilterActionOptionsBinding binging = DataBindingUtil.inflate(
                getLayoutInflater(),
                R.layout.filter_action_options,
                null,
                false
        );
        switch (currentOrder) {
            case ASCENDING:
                binging.rbSortOrderAscending.setChecked(true);
                break;
            case DESCENDING:
                binging.rbSortOrderDescending.setChecked(true);
                break;
        }

        switch (currentOption) {
//            case DATE_ADDED:
//                binging.rbSortOptionAddedDate.setChecked(true);
//                break;
            case DATE_FROZEN_AT:
                binging.rbSortOptionFrozenAt.setChecked(true);
                break;
            case DATE_BEST_BEFORE:
                binging.rbSortOptionBestBefore.setChecked(true);
                break;
            case NAME:
                binging.rbSortOptionName.setChecked(true);
                break;
        }

        binging.rbSortOrderAscending.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentOrder = SortingOption.SortingOrder.ASCENDING;
            }
        });
        binging.rbSortOrderDescending.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentOrder = SortingOption.SortingOrder.DESCENDING;
            }
        });

        binging.rbSortOptionName.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentOption = SortingOption.NAME;
            }
        });
//        binging.rbSortOptionAddedDate.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            if (isChecked) {
//                currentOption = SortingOption.DATE_ADDED;
//            }
//        });
        binging.rbSortOptionBestBefore.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentOption = SortingOption.DATE_BEST_BEFORE;
            }
        });
        binging.rbSortOptionFrozenAt.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentOption = SortingOption.DATE_FROZEN_AT;
            }
        });

        return new AlertDialog.Builder(context)
                .setView(binging.getRoot())
                .setPositiveButton("Select", (dialog, which) -> {
                    listener.listOptionClicked(currentOption, currentOrder);
                })
                .setNegativeButton("Close", (dialog, which) -> {
                    // Noop
                })
                .create();
    }
}
