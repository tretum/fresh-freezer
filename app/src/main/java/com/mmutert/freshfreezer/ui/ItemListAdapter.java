package com.mmutert.freshfreezer.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mmutert.freshfreezer.data.FrozenItem;
import com.mmutert.freshfreezer.databinding.ListItemBinding;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;

public class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.ItemListAdapterViewHolder> {

    private final ListItemClickedCallback itemClickedCallback;
    private ListItemDeleteClickedCallback deleteClickedCallback;
    private ListItemTakeClickedCallback takeClickedCallback;
    private List<FrozenItem> mItems;


    public ItemListAdapter(
            ListItemClickedCallback itemClickedCallback,
            ListItemDeleteClickedCallback deleteClickedCallback,
            ListItemTakeClickedCallback takeClickedCallback) {
        this.itemClickedCallback = itemClickedCallback;
        this.deleteClickedCallback = deleteClickedCallback;
        this.takeClickedCallback = takeClickedCallback;
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
        FrozenItem itemForPosition = getItemAtPosition(position);

        binding.setItem(itemForPosition);

        LocalDate bestBeforeDate = itemForPosition.getBestBeforeDate();
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        String bestBeforeFormatted = formatter.print(bestBeforeDate);
        LocalDate frozenDate = itemForPosition.getFrozenAtDate();
        String frozenFormatted = formatter.print(frozenDate);

        binding.tvBestBeforeDate.setText(bestBeforeFormatted);
        binding.tvDateFrozen.setText(frozenFormatted);

        // Set up the delete button
        binding.btDelete.setOnClickListener(v -> {
            deleteClickedCallback.onDeleteClicked(itemForPosition, position);
        });

        binding.btTake.setOnClickListener(v -> {
            takeClickedCallback.onTakeButtonClicked(itemForPosition);
        });

        binding.getRoot().setOnClickListener(v -> {
            itemClickedCallback.onClick(itemForPosition);
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

    public FrozenItem getItemAtPosition(int position) {
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
