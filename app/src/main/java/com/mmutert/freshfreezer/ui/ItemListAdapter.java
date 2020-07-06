package com.mmutert.freshfreezer.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mmutert.freshfreezer.data.FrozenItem;
import com.mmutert.freshfreezer.databinding.ListItemBinding;
import com.mmutert.freshfreezer.util.SortingOption;
import com.mmutert.freshfreezer.viewmodel.FrozenItemViewModel;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Collections;
import java.util.List;


public class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.ItemListAdapterViewHolder> implements
        ListSortingDialogFragment.ListSortingChangedListener {

    private final ListItemClickedCallback itemClickedCallback;
    private ListItemDeleteClickedCallback deleteClickedCallback;
    private ListItemTakeClickedCallback takeClickedCallback;
    private List<FrozenItem> mItems;
    private FrozenItemViewModel mViewModel;


    public ItemListAdapter(
            final FrozenItemViewModel viewModel,
            ListItemClickedCallback itemClickedCallback,
            ListItemDeleteClickedCallback deleteClickedCallback,
            ListItemTakeClickedCallback takeClickedCallback) {
        this.mViewModel            = viewModel;
        this.itemClickedCallback   = itemClickedCallback;
        this.deleteClickedCallback = deleteClickedCallback;
        this.takeClickedCallback   = takeClickedCallback;
    }


    public void setItems(List<FrozenItem> items) {
        this.mItems = items;
        sortItems();
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
        if (mItems == null) {
            return 0;
        } else {
            return mItems.size();
        }
    }

    public FrozenItem getItemAtPosition(int position) {
        return mItems.get(position);
    }

    @Override
    public void listOptionClicked(
            final SortingOption selectedSortingOption, final SortingOption.SortingOrder sortingOrder) {
        mViewModel.setSortingOption(selectedSortingOption);
        mViewModel.setSortingOrder(sortingOrder);

        sortItems();
    }

    private void sortItems() {
        if (mItems.size() > 0) {
            switch (mViewModel.getSortingOption()) {
                // TODO Add sorting for added date
//                case DATE_ADDED:
//                    break;
                case DATE_FROZEN_AT:
                    Collections.sort(mItems, (item1, item2) -> {
                        int result = item1.getFrozenAtDate().compareTo(item2.getFrozenAtDate());
                        if(mViewModel.getSortingOrder().equals(SortingOption.SortingOrder.ASCENDING)){
                            return result;
                        } else {
                            return result * (-1);
                        }
                    });
                    break;
                case DATE_BEST_BEFORE:
                    Collections.sort(mItems, (item1, item2) -> {
                        int result = item1.getBestBeforeDate().compareTo(item2.getBestBeforeDate());
                        if(mViewModel.getSortingOrder().equals(SortingOption.SortingOrder.ASCENDING)){
                            return result;
                        } else {
                            return result * (-1);
                        }
                    });
                    break;
                case NAME:
                    Collections.sort(mItems, (item1, item2) -> {
                        int result = item1.getName().compareTo(item2.getName());
                        if(mViewModel.getSortingOrder().equals(SortingOption.SortingOrder.ASCENDING)){
                            return result;
                        } else {
                            return result * (-1);
                        }
                    });
                    break;
            }
        }
        notifyDataSetChanged();
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
