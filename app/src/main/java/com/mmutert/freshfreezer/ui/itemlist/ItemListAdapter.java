package com.mmutert.freshfreezer.ui.itemlist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.mmutert.freshfreezer.data.AmountUnit;
import com.mmutert.freshfreezer.data.FrozenItem;
import com.mmutert.freshfreezer.databinding.ListItemBinding;
import com.mmutert.freshfreezer.ui.ListSortingDialogFragment;
import com.mmutert.freshfreezer.util.SortingOption;
import com.mmutert.freshfreezer.viewmodel.FrozenItemViewModel;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


public class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.ItemListAdapterViewHolder> implements
        ListSortingDialogFragment.ListSortingChangedListener {

    private final AsyncListDiffer<FrozenItem> mDiffer = new AsyncListDiffer<>(this, DIFF_CALLBACK);

    private final ListItemClickedCallback itemClickedCallback;
    private Context context;
    private FrozenItemViewModel mViewModel;


    public ItemListAdapter(
            final FrozenItemViewModel viewModel,
            ListItemClickedCallback itemClickedCallback,
            Context context) {
        this.mViewModel            = viewModel;
        this.itemClickedCallback   = itemClickedCallback;
        this.context               = context;
    }


    public void setItems(List<FrozenItem> items) {
        ArrayList<FrozenItem> newItems = new ArrayList<>(items);
        sortItems(newItems);
        mDiffer.submitList(newItems);
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

        // TODO Format tv_amount
        AmountUnit unit = itemForPosition.getUnit();
        binding.tvAmountUnit.setText(context.getResources().getString(unit.getStringResId()));

        NumberFormat numberInstance = AmountUnit.getFormatterForUnit(itemForPosition.getUnit());
        String amountText = numberInstance.format(itemForPosition.getAmount());
        binding.tvAmount.setText(amountText);

        DateTimeFormatter formatter = DateTimeFormat.longDate().withLocale(Locale.getDefault());

        LocalDate bestBeforeDate = itemForPosition.getBestBeforeDate();
        String bestBeforeFormatted = formatter.print(bestBeforeDate);
        binding.tvBestBeforeDate.setText(bestBeforeFormatted);

        LocalDate frozenDate = itemForPosition.getFrozenAtDate();
        if(frozenDate != null) {
            binding.tvDateFrozen.setVisibility(View.VISIBLE);
            binding.tvFrozenDateTitle.setVisibility(View.VISIBLE);
            String frozenFormatted = formatter.print(frozenDate);
            binding.tvDateFrozen.setText(frozenFormatted);
        } else {
            binding.tvFrozenDateTitle.setVisibility(View.GONE);
            binding.tvDateFrozen.setVisibility(View.GONE);
        }
        binding.getRoot().setOnClickListener(v -> {
            itemClickedCallback.onClick(itemForPosition);
        });

        binding.listItemDeleteBackground.setVisibility(View.INVISIBLE);
        binding.listItemTakeBackground.setVisibility(View.INVISIBLE);
    }

    @Override
    public int getItemCount() {
        return mDiffer.getCurrentList().size();
    }

    public FrozenItem getItemAtPosition(int position) {
        return mDiffer.getCurrentList().get(position);
    }

    @Override
    public void listOptionClicked(
            final SortingOption selectedSortingOption, final SortingOption.SortingOrder sortingOrder) {
        mViewModel.setSortingOption(selectedSortingOption);
        mViewModel.setSortingOrder(sortingOrder);

        ArrayList<FrozenItem> frozenItems = new ArrayList<>(mDiffer.getCurrentList());
        sortItems(frozenItems);
        mDiffer.submitList(frozenItems);
    }

    private void sortItems(final List<FrozenItem> items) {
        if (items.size() > 0) {
            switch (mViewModel.getSortingOption()) {
                case DATE_CHANGED:
                    Collections.sort(items, (item1, item2) -> {
                        int result = item1.getLastChangedAtDate().compareTo(item2.getLastChangedAtDate());
                        if (mViewModel.getSortingOrder().equals(SortingOption.SortingOrder.ASCENDING)) {
                            return result;
                        } else {
                            return result * (-1);
                        }
                    });
                    break;
                case DATE_ADDED:
                    Collections.sort(items, (item1, item2) -> {
                        int result = item1.getItemCreationDate().compareTo(item2.getItemCreationDate());
                        if (mViewModel.getSortingOrder().equals(SortingOption.SortingOrder.ASCENDING)) {
                            return result;
                        } else {
                            return result * (-1);
                        }
                    });
                    break;
                case DATE_FROZEN_AT:
                    Collections.sort(items, (item1, item2) -> {
                        int result = item1.getFrozenAtDate().compareTo(item2.getFrozenAtDate());
                        if(mViewModel.getSortingOrder().equals(SortingOption.SortingOrder.ASCENDING)){
                            return result;
                        } else {
                            return result * (-1);
                        }
                    });
                    break;
                case DATE_BEST_BEFORE:
                    Collections.sort(items, (item1, item2) -> {
                        int result = item1.getBestBeforeDate().compareTo(item2.getBestBeforeDate());
                        if(mViewModel.getSortingOrder().equals(SortingOption.SortingOrder.ASCENDING)){
                            return result;
                        } else {
                            return result * (-1);
                        }
                    });
                    break;
                case NAME:
                    Collections.sort(items, (item1, item2) -> {
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
    }

    /**
     * The Callback for the DiffUtil.
     */
    private static final DiffUtil.ItemCallback<FrozenItem> DIFF_CALLBACK
            = new DiffUtil.ItemCallback<FrozenItem>() {
        @Override
        public boolean areItemsTheSame(
                @NonNull FrozenItem oldFrozenItem, @NonNull FrozenItem newFrozenItem) {
            // FrozenItem properties may have changed if reloaded from the DB, but ID is fixed
            return oldFrozenItem.getId() == newFrozenItem.getId();
        }
        @Override
        public boolean areContentsTheSame(
                @NonNull FrozenItem oldFrozenItem, @NonNull FrozenItem newFrozenItem) {
            // NOTE: if you use equals, your object must properly override Object#equals()
            // Incorrectly returning false here will result in too many animations.
            return oldFrozenItem.equals(newFrozenItem);
        }
    };

    public List<FrozenItem> getItemList() {
        return mDiffer.getCurrentList();
    }


    /**
     * The view holder for the &
     */
    public class ItemListAdapterViewHolder extends RecyclerView.ViewHolder {

        public final ListItemBinding binding;

        public ItemListAdapterViewHolder(ListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
