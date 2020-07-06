package com.mmutert.freshfreezer.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mmutert.freshfreezer.R;
import com.mmutert.freshfreezer.databinding.DialogAddNotificationBinding;


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

        DialogAddNotificationBinding dialogBinding = DataBindingUtil.inflate(
                getLayoutInflater(),
                R.layout.dialog_add_notification,
                null,
                false
        );
        dialogBinding.radioButtonDays.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                mOffsetAmount = PendingNotification.OffsetAmount.DAYS;
            }
            setBeforeText(buttonView, isChecked);
        });
        dialogBinding.radioButtonWeeks.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                mOffsetAmount = PendingNotification.OffsetAmount.WEEKS;
            }
            setBeforeText(buttonView, isChecked);
        });
        dialogBinding.radioButtonMonths.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                mOffsetAmount = PendingNotification.OffsetAmount.MONTHS;
            }
            setBeforeText(buttonView, isChecked);
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

    private void setBeforeText(CompoundButton rb, boolean isChecked) {
        String text = rb.getText().toString();
        if (isChecked) {
            rb.setText(text + " before");
        } else {
            int index = text.indexOf(" before");
            if (index != -1) {
                rb.setText(text.substring(0, index));
            }
        }
    }

    public int getEnteredOffset() {
        return Integer.parseInt(mNotificationOffsetEditText.getText().toString());
    }

    public PendingNotification.OffsetAmount getOffSetAmount() {
        return mOffsetAmount;
    }
}
