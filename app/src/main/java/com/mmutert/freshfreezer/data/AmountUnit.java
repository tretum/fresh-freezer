package com.mmutert.freshfreezer.data;

import com.mmutert.freshfreezer.R;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.NumberFormat;
import java.util.Locale;


public enum AmountUnit {
    GRAMS(R.string.unit_grams),
    KILOGRAMS(R.string.unit_kilograms),
    PIECES(R.string.unit_pieces),
    LITERS(R.string.unit_liters),
    MILLILITERS(R.string.unit_milliliters);

    private final int stringResId;

    AmountUnit(int stringResId) {
        this.stringResId = stringResId;
    }

    public int getStringResId() {
        return stringResId;
    }

    public static NumberFormat getFormatterForUnit(AmountUnit unit) {
        NumberFormat numberInstance = NumberFormat.getNumberInstance();
        switch (unit) {
            case KILOGRAMS:
            case LITERS:
                numberInstance.setMaximumFractionDigits(3);
                break;

            case GRAMS:
            case PIECES:
            case MILLILITERS:
                numberInstance.setMaximumFractionDigits(1);
                break;
        }
        return numberInstance;
    }
}
