package com.mmutert.freshfreezer.ui;

import android.content.Context;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import com.mmutert.freshfreezer.data.AmountUnit;
import com.mmutert.freshfreezer.data.Condition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ConditionArrayAdapter extends ArrayAdapter<CharSequence>{

    private final List<CharSequence> values;

    public ConditionArrayAdapter(@NonNull final Context context, final int resource) {
        super(context, resource);

        values = new ArrayList<>();
        for (Condition value : Condition.values()) {
            String valueString = context.getResources().getString(value.getStringResId());
            values.add(valueString);
        }
        super.addAll(values);
    }

    public Condition getSelectedUnit(final int position) {
        return Condition.values()[position];
    }

    public int getIndexOfUnit(Condition unit) {
        return Arrays.asList(Condition.values()).indexOf(unit);
    }
}
