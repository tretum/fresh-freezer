package com.mmutert.freshfreezer.ui.databinding

import androidx.databinding.InverseMethod

object AmountBindingConverter {
    @InverseMethod("stringToFloat")
    fun floatToString(newValue: Float): String {
        return newValue.toString()
    }

    fun stringToFloat(newValue: String): Float {
        return try {
            newValue.toFloat()
        } catch (e: NumberFormatException) {
            0.0f
        }
    }
}