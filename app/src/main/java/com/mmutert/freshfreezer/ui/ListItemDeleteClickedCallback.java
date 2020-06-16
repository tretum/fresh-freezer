package com.mmutert.freshfreezer.ui;

import com.mmutert.freshfreezer.data.FrozenItem;

public interface ListItemDeleteClickedCallback {

    void onDeleteClicked(FrozenItem itemToDelete, int position);
}
