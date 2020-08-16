package com.mmutert.freshfreezer.data;

import com.mmutert.freshfreezer.R;


public enum Condition {
    FROZEN(R.string.condition_frozen),
    CHILLED(R.string.condition_chilled),
    ROOM_TEMP(R.string.condition_room_temp);

    private final int stringResId;

    Condition(int stringResId) {
        this.stringResId = stringResId;
    }

    public int getStringResId() {
        return stringResId;
    }
}
