package com.mmutert.freshfreezer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.ItemListAdapterViewHolder> {


    @NonNull
    @Override
    public ItemListAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_item, parent);
        ItemListAdapterViewHolder viewHolder = new ItemListAdapterViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ItemListAdapterViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class ItemListAdapterViewHolder extends RecyclerView.ViewHolder {

        private TextView mTitleTextView;
        private TextView mFrozenDateTextView;
        private TextView mBestBeforeTextView;
        private TextView mAmountTextView;

        public ItemListAdapterViewHolder(@NonNull View itemView) {
            super(itemView);

            mTitleTextView = itemView.findViewById(R.id.tv_title);
            mFrozenDateTextView = itemView.findViewById(R.id.tv_date_frozen);
            mAmountTextView = itemView.findViewById(R.id.tv_amount);
            mBestBeforeTextView = itemView.findViewById(R.id.tv_best_before_date);
        }
    }
}
