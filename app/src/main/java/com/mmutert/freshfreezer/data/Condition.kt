package com.mmutert.freshfreezer.data

import com.mmutert.freshfreezer.R

enum class Condition(val stringResId: Int) {
    FROZEN(R.string.condition_frozen), CHILLED(R.string.condition_chilled), ROOM_TEMP(R.string.condition_room_temp);
}