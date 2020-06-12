package com.mmutert.freshfreezer.ui;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mmutert.freshfreezer.data.FrozenItem;
import com.mmutert.freshfreezer.databinding.ListItemBinding;

import java.util.Date;
import java.util.List;

public class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.ItemListAdapterViewHolder> {

    private final ListItemClickedCallback callback;
    private ListItemDeleteClickedCallback deleteClickedCallback;
    private List<FrozenItem> mItems;


    public ItemListAdapter(ListItemClickedCallback callback, ListItemDeleteClickedCallback deleteClickedCallback) {
        this.callback = callback;
        this.deleteClickedCallback = deleteClickedCallback;
    }


    public void setItems(List<FrozenItem> items) {
        this.mItems = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemListAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        ListItemBinding binding = ListItemBinding.inflate(inflater, parent, false);

        return new ItemListAdapterViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemListAdapterViewHolder holder, int position) {
        ListItemBinding binding = holder.binding;
        FrozenItem itemForPosition = getItemForPosition(position);

        binding.setItem(itemForPosition);

        Date bestBeforeDate = itemForPosition.getBestBeforeDate();
        String bestBeforeFormatted = DateFormat.format("yyyy-MM-dd", bestBeforeDate).toString();
        Date frozenDate = itemForPosition.getFrozenDate();
        String frozenFormatted = DateFormat.format("yyyy-MM-dd", frozenDate).toString();

        binding.tvBestBeforeDate.setText(bestBeforeFormatted);
        binding.tvDateFrozen.setText(frozenFormatted);

        // Set up the delete button
        binding.btDelete.setOnClickListener(v -> {
            deleteClickedCallback.onDeleteClicked(getItemForPosition(position));
        });
    }

    @Override
    public int getItemCount() {
        if(mItems == null) {
            return 0;
        } else {
            return mItems.size();
        }
    }

    private FrozenItem getItemForPosition(int position) {
        return mItems.get(position);
    }


    /**
     *
     */
    public static class ItemListAdapterViewHolder extends RecyclerView.ViewHolder {

        private final ListItemBinding binding;

        public ItemListAdapterViewHolder(ListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
