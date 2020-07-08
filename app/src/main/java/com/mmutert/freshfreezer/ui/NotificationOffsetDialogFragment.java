package com.mmutert.freshfreezer.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.CompoundButton;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mmutert.freshfreezer.R;
import com.mmutert.freshfreezer.databinding.DialogAddNotificationBinding;
import com.mmutert.freshfreezer.ui.databinding.IntStringConverter;

import java.util.Objects;


public class NotificationOffsetDialogFragment extends DialogFragment {

    private PendingNotification.OffsetUnit mOffsetUnit = PendingNotification.OffsetUnit.DAYS;
    private DialogAddNotificationBinding dialogBinding;

    private int selectedValue = 1;


    public interface NotificationOffsetDialogClickListener {
        void onPositiveClick(NotificationOffsetDialogFragment dialog);
    }


    private final NotificationOffsetDialogClickListener listener;

    public NotificationOffsetDialogFragment(NotificationOffsetDialogClickListener listener) {
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(@Nullable final Bundle savedInstanceState) {

        dialogBinding = DataBindingUtil.inflate(
                getLayoutInflater(),
                R.layout.dialog_add_notification,
                null,
                false
        );

        setupRadioButtons();

        // Initialize the selected offset after setting up the radio buttons to correctly set the text for the labels
        dialogBinding.setSelectedOffset(1);

        return new MaterialAlertDialogBuilder(getContext())
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Done", (dialog, which) -> {
                    listener.onPositiveClick(this);
                })
                .create();
    }

    private void setupRadioButtons() {
        CompoundButton.OnCheckedChangeListener daysButtonListener = (buttonView, isChecked) -> {

            if (isChecked) {
                mOffsetUnit = PendingNotification.OffsetUnit.DAYS;

                String quantityString = getResources().getQuantityString(
                        R.plurals.days_selected_capitalized_plural,
                        selectedValue
                );
                buttonView.setText(quantityString);
            } else {
                String quantityString = getResources().getQuantityString(
                        R.plurals.days_capitalized_plural,
                        selectedValue
                );
                buttonView.setText(quantityString);
            }
        };

        CompoundButton.OnCheckedChangeListener weeksButtonListener = (buttonView, isChecked) -> {
            //            Integer amount = IntStringConverter.stringToInt(dialogBinding.etAddNotificationOffsetAmount.getText().toString());

            if (isChecked) {
                mOffsetUnit = PendingNotification.OffsetUnit.WEEKS;

                String quantityString = getResources().getQuantityString(
                        R.plurals.weeks_selected_capitalized_plural,
                        selectedValue
                );
                buttonView.setText(quantityString);
            } else {
                String quantityString = getResources().getQuantityString(
                        R.plurals.weeks_capitalized_plural,
                        selectedValue
                );
                buttonView.setText(quantityString);
            }
        };

        CompoundButton.OnCheckedChangeListener monthsButtonListener = (buttonView, isChecked) -> {
            if (isChecked) {
                mOffsetUnit = PendingNotification.OffsetUnit.MONTHS;

                String quantityString = getResources().getQuantityString(
                        R.plurals.months_selected_capitalized_plural,
                        selectedValue
                );
                buttonView.setText(quantityString);
            } else {
                String quantityString = getResources().getQuantityString(
                        R.plurals.months_capitalized_plural,
                        selectedValue
                );
                buttonView.setText(quantityString);
            }
        };

        dialogBinding.etAddNotificationOffsetAmount.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {

            }

            @Override
            public void afterTextChanged(final Editable s) {
                int value = IntStringConverter.stringToInt(s.toString());

                if (!Objects.equals(selectedValue, value)) {
                    selectedValue = value;
                }
                daysButtonListener.onCheckedChanged(dialogBinding.radioButtonDays, dialogBinding.radioButtonDays.isChecked());
                weeksButtonListener.onCheckedChanged(dialogBinding.radioButtonWeeks, dialogBinding.radioButtonWeeks.isChecked());
                monthsButtonListener.onCheckedChanged(dialogBinding.radioButtonMonths, dialogBinding.radioButtonMonths.isChecked());
            }
        });


        dialogBinding.radioButtonDays.setOnCheckedChangeListener(daysButtonListener);
        dialogBinding.radioButtonWeeks.setOnCheckedChangeListener(weeksButtonListener);
        dialogBinding.radioButtonMonths.setOnCheckedChangeListener(monthsButtonListener);
    }

    public int getEnteredOffset() {
        return dialogBinding.getSelectedOffset();
    }

    public PendingNotification.OffsetUnit getOffSetAmount() {
        return mOffsetUnit;
    }
}
