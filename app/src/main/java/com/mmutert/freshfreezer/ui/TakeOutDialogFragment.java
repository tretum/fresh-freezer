package com.mmutert.freshfreezer.ui;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mmutert.freshfreezer.R;
import com.mmutert.freshfreezer.data.AmountUnit;
import com.mmutert.freshfreezer.data.FrozenItem;
import com.mmutert.freshfreezer.databinding.DialogTakeItemBinding;

import java.text.NumberFormat;


public class TakeOutDialogFragment extends DialogFragment {

    private FrozenItem item;
    private DialogTakeItemBinding binding;


    public interface TakeOutDialogClickListener {
        void onPositiveClicked(TakeOutDialogFragment dialog);

        void onTakeAllClicked(TakeOutDialogFragment dialog);

        void onCancelClicked(TakeOutDialogFragment dialog);
    }


    private final TakeOutDialogClickListener listener;

    public TakeOutDialogFragment(final TakeOutDialogClickListener listener, FrozenItem item) {
        this.listener = listener;
        this.item     = item;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable final Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(
                getLayoutInflater(),
                R.layout.dialog_take_item,
                null,
                false
        );
        binding.setItem(this.item);

        String unitString = getContext().getResources().getString(item.getUnit().getStringResId());
        binding.takeDialogUnit.setText(unitString);
        NumberFormat numberInstance = AmountUnit.getFormatterForUnit(item.getUnit());
        String amountAsString = numberInstance.format(item.getAmount());
        binding.takeDialogCurrentAmount.setText(amountAsString + " " + unitString);

        return new MaterialAlertDialogBuilder(getContext())
                .setView(binding.getRoot())
                .setPositiveButton(
                        R.string.dialog_take_accept_button_label,
                        (dialog, which) -> listener.onPositiveClicked(this)
                )
                .setNeutralButton(
                        R.string.dialog_take_button_take_all_label,
                        (dialog, which) -> listener.onTakeAllClicked(this)
                )
                .setNegativeButton(
                        R.string.dialog_take_button_cancel_label,
                        (dialog, which) -> listener.onCancelClicked(this)
                )
                .create();
    }

    public float getSelectionAmount() {
        try {
            return Float.parseFloat(binding.etTakeDialogSelectedAmount.getText().toString());
        } catch (NumberFormatException e) {
            return 0F;
        }
    }

    public FrozenItem getItem() {
        return item;
    }
}
