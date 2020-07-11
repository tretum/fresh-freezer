package com.mmutert.freshfreezer.data;

import com.mmutert.freshfreezer.R;


public enum AmountUnit {
    GRAMS(R.string.unit_grams),
    KILOGRAMS(R.string.unit_kilograms),
    PIECES(R.string.unit_pieces),
    LITERS(R.string.unit_liters),
    MILLILITERS(R.string.unit_milliliters);

    private int stringResId;

    AmountUnit(int stringResId) {
        this.stringResId = stringResId;
    }

    public int getStringResId() {
        return stringResId;
    }
}
