package com.mmutert.freshfreezer.data

import com.mmutert.freshfreezer.R
import java.text.NumberFormat

enum class AmountUnit(val stringResId: Int) {
    GRAMS(R.string.unit_grams),
    KILOGRAMS(R.string.unit_kilograms),
    PIECES(R.string.unit_pieces),
    LITERS(R.string.unit_liters),
    MILLILITERS(R.string.unit_milliliters);

    companion object {
        @JvmStatic
        fun getFormatterForUnit(unit: AmountUnit): NumberFormat {
            val numberInstance = NumberFormat.getNumberInstance()
            when (unit) {
                KILOGRAMS, LITERS -> numberInstance.maximumFractionDigits = 3
                GRAMS, PIECES, MILLILITERS -> numberInstance.maximumFractionDigits = 1
            }
            return numberInstance
        }
    }
}