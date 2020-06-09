package com.mmutert.freshfreezer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class FrozenItemListFragment extends Fragment {

    private RecyclerView mItemListRecyclerView;


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_frozen_item_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mItemListRecyclerView = view.findViewById(R.id.rv_frozen_item_list);

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(view.getContext(), LinearLayoutManager.VERTICAL, false);
        mItemListRecyclerView.setLayoutManager(layoutManager);

        ItemListAdapter itemListAdapter = new ItemListAdapter();
        mItemListRecyclerView.setAdapter(itemListAdapter);

        final FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(view2 -> {
            fab.setVisibility(View.GONE);
            Navigation.findNavController(view).navigate(R.id.action_FirstFragment_to_addItemFragment);
        });
    }
}
