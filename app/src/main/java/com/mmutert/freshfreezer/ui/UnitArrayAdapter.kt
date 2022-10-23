package com.mmutert.freshfreezer.ui

import android.content.Context
import android.widget.ArrayAdapter
import com.mmutert.freshfreezer.data.AmountUnit

class UnitArrayAdapter(context: Context, resource: Int) :
    ArrayAdapter<CharSequence?>(context, resource) {

    private val values: MutableList<CharSequence> = ArrayList()

    fun getSelectedUnit(position: Int): AmountUnit {
        return AmountUnit.values()[position]
    }

    fun getIndexOfUnit(unit: AmountUnit): Int {
        return AmountUnit.values().indexOf(unit)
    }

    init {
        for (value in AmountUnit.values()) {
            val valueString = context.resources.getString(value.stringResId)
            values.add(valueString)
        }
        super.addAll(values)
    }
}