package com.mmutert.freshfreezer.ui;

import android.content.Context;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import com.mmutert.freshfreezer.data.AmountUnit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class UnitArrayAdapter extends ArrayAdapter<CharSequence>{

    private final List<CharSequence> values;

    public UnitArrayAdapter(@NonNull final Context context, final int resource) {
        super(context, resource);

        values = new ArrayList<>();
        for (AmountUnit value : AmountUnit.values()) {
            String valueString = context.getResources().getString(value.getStringResId());
            values.add(valueString);
        }
        super.addAll(values);
    }

    public AmountUnit getSelectedUnit(final int position) {
        return AmountUnit.values()[position];
    }

    public int getIndexOfUnit(AmountUnit unit) {
        return Arrays.asList(AmountUnit.values()).indexOf(unit);
    }
}
