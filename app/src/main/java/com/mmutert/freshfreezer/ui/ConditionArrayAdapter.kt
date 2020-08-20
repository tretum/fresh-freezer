package com.mmutert.freshfreezer.ui

import android.content.Context
import android.widget.ArrayAdapter
import com.mmutert.freshfreezer.data.Condition
import java.util.*

class ConditionArrayAdapter(context: Context, resource: Int) :
        ArrayAdapter<CharSequence?>(context, resource) {

    private val values: MutableList<CharSequence> = ArrayList()

    fun getSelectedUnit(position: Int): Condition {
        return Condition.values()[position]
    }

    fun getIndexOfUnit(unit: Condition): Int {
        return Condition.values().indexOf(unit)
    }

    init {
        for (value in Condition.values()) {
            val valueString = context.resources.getString(value.stringResId)
            values.add(valueString)
        }
        super.addAll(values)
    }
}