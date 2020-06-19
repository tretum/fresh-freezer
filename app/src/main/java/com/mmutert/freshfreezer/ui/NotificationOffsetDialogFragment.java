package com.mmutert.freshfreezer.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mmutert.freshfreezer.R;
import com.mmutert.freshfreezer.databinding.AddNotificationDialogBinding;


public class NotificationOffsetDialogFragment extends DialogFragment {

    private EditText mNotificationOffsetEditText;
    private PendingNotification.OffsetAmount mOffsetAmount;


    public interface NotificationOffsetDialogClickListener {
        void onPositiveClick(NotificationOffsetDialogFragment dialog);
    }


    private final NotificationOffsetDialogClickListener listener;

    public NotificationOffsetDialogFragment(NotificationOffsetDialogClickListener listener) {
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(@Nullable final Bundle savedInstanceState) {

        AddNotificationDialogBinding dialogBinding = DataBindingUtil.inflate(
                getLayoutInflater(),
                R.layout.add_notification_dialog,
                null,
                false
        );
        dialogBinding.radioButtonDays.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                mOffsetAmount = PendingNotification.OffsetAmount.DAYS;
            }
        });
        dialogBinding.radioButtonWeeks.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                mOffsetAmount = PendingNotification.OffsetAmount.WEEKS;
            }
        });
        dialogBinding.radioButtonMonths.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                mOffsetAmount = PendingNotification.OffsetAmount.MONTHS;
            }
        });

        mNotificationOffsetEditText = dialogBinding.etAddNotificationOffsetAmount;

        AlertDialog alertDialog = new MaterialAlertDialogBuilder(getContext())
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Done", (dialog, which) -> {
                    listener.onPositiveClick(this);
                })
                .create();
        return alertDialog;
    }

    public int getEnteredOffset() {
        return Integer.parseInt(mNotificationOffsetEditText.getText().toString());
    }

    public PendingNotification.OffsetAmount getOffSetAmount() {
        return mOffsetAmount;
    }
}
