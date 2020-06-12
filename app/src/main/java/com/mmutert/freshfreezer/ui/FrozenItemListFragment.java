package com.mmutert.freshfreezer.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.mmutert.freshfreezer.R;
import com.mmutert.freshfreezer.data.FrozenItem;
import com.mmutert.freshfreezer.databinding.FragmentFrozenItemListBinding;
import com.mmutert.freshfreezer.viewmodel.FrozenItemViewModel;

public class FrozenItemListFragment extends Fragment implements ListItemClickedCallback{

    private FragmentFrozenItemListBinding mBinding;


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        mBinding = FragmentFrozenItemListBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(view.getContext(), LinearLayoutManager.VERTICAL, false);
        mBinding.rvFrozenItemList.setLayoutManager(layoutManager);

        ItemListAdapter itemListAdapter = new ItemListAdapter(this);
        mBinding.rvFrozenItemList.setAdapter(itemListAdapter);

        FrozenItemViewModel viewModel = new ViewModelProvider(this).get(FrozenItemViewModel.class);
        viewModel.getFrozenItems().observe(getViewLifecycleOwner(), itemListAdapter::setItems);

        mBinding.fab.setOnClickListener(view2 -> {
            mBinding.fab.setVisibility(View.GONE);
            Navigation.findNavController(view).navigate(R.id.action_FirstFragment_to_addItemFragment);
            Log.d("", "Clicked FAB");
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onClick(FrozenItem item) {

    }
}
