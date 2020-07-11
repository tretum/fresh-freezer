package com.mmutert.freshfreezer.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mmutert.freshfreezer.R;
import com.mmutert.freshfreezer.databinding.DialogFilterActionOptionsBinding;
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

        DialogFilterActionOptionsBinding binging = DataBindingUtil.inflate(
                getLayoutInflater(),
                R.layout.dialog_filter_action_options,
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
            case DATE_ADDED:
                binging.rbSortOptionAddedDate.setChecked(true);
                break;
            case DATE_CHANGED:
                binging.rbSortOptionLastChangedDate.setChecked(true);
                break;
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

        binging.rbSortOptionAddedDate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentOption = SortingOption.DATE_ADDED;
            }
        });

        binging.rbSortOptionLastChangedDate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentOption = SortingOption.DATE_CHANGED;
            }
        });

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

        return new MaterialAlertDialogBuilder(context)
                .setView(binging.getRoot())
                .setPositiveButton(R.string.dialog_sort_select_button_label, (dialog, which) -> {
                    listener.listOptionClicked(currentOption, currentOrder);
                })
                .setNegativeButton(R.string.dialog_sort_close_button_label, (dialog, which) -> {
                    // Noop
                })
                .create();
    }
}
