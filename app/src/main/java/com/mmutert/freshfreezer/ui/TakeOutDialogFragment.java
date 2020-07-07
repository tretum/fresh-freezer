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
import com.mmutert.freshfreezer.data.FrozenItem;
import com.mmutert.freshfreezer.databinding.DialogTakeItemBinding;


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

        AlertDialog alertDialog = new MaterialAlertDialogBuilder(getContext())
                .setView(binding.getRoot())
                .setPositiveButton("Ok", (dialog, which) -> listener.onPositiveClicked(this))
                .setNeutralButton("Take all", (dialog, which) -> listener.onTakeAllClicked(this))
                .setNegativeButton("Cancel", (dialog, which) -> listener.onCancelClicked(this))
                .create();

        return alertDialog;
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
