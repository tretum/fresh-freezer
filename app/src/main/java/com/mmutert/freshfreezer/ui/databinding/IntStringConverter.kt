package com.mmutert.freshfreezer.ui.databinding

import androidx.databinding.InverseMethod

object IntStringConverter {
    @InverseMethod("stringToInt")
    fun intToString(toConvert: Int): String {
        return toConvert.toString()
    }

    fun stringToInt(toConvert: String): Int {
        return try {
            toConvert.toInt()
        } catch (e: NumberFormatException) {
            0
        }
    }
}