package com.mmutert.freshfreezer.ui;

import android.app.Notification;
import android.os.Bundle;
import android.renderscript.RenderScript;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.mmutert.freshfreezer.R;
import com.mmutert.freshfreezer.data.FrozenItem;
import com.mmutert.freshfreezer.databinding.FragmentFrozenItemListBinding;
import com.mmutert.freshfreezer.viewmodel.FrozenItemViewModel;

import static com.mmutert.freshfreezer.notification.NotificationConstants.CHANNEL_ID;


public class FrozenItemListFragment extends Fragment
        implements ListItemClickedCallback, ListItemDeleteClickedCallback, ListItemTakeClickedCallback {

    private FragmentFrozenItemListBinding mBinding;
    private FrozenItemViewModel mViewModel;
    private ItemListAdapter mItemListAdapter;


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
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

        mItemListAdapter = new ItemListAdapter(this, this, this);
        mBinding.rvFrozenItemList.setAdapter(mItemListAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.RIGHT
        ) {
            @Override
            public boolean onMove(
                    @NonNull final RecyclerView recyclerView,
                    @NonNull final RecyclerView.ViewHolder viewHolder,
                    @NonNull final RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, final int direction) {
                // Delete the item
                int pos = viewHolder.getAdapterPosition();
                archiveItem(mItemListAdapter.getItemAtPosition(pos), pos);
            }
        });
        itemTouchHelper.attachToRecyclerView(mBinding.rvFrozenItemList);

        mViewModel = new ViewModelProvider(this).get(FrozenItemViewModel.class);
        mViewModel.getFrozenItems().observe(getViewLifecycleOwner(), mItemListAdapter::setItems);

        mBinding.fab.setOnClickListener(view2 -> {
            mBinding.fab.setVisibility(View.GONE);
            Navigation.findNavController(view).navigate(R.id.action_item_list_to_add_item);
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
        Log.d("ListFragment", "Clicked on item " + item.getName());
    }


    private void archiveItem(FrozenItem itemToArchive, int position) {
        mItemListAdapter.notifyItemRemoved(position);

        FrozenItemViewModel viewModel = new ViewModelProvider(this).get(FrozenItemViewModel.class);
        Snackbar snackbar = Snackbar.make(
                mBinding.itemListCoordinatorLayout,
                "Deleted item " + itemToArchive.getName(),
                Snackbar.LENGTH_LONG
        );
        snackbar.setAction("Undo", v -> {
            viewModel.restore(itemToArchive);
            mItemListAdapter.notifyItemRangeInserted(position, 1);
        });
        snackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE);
        snackbar.show();
        viewModel.archive(itemToArchive);
    }

    @Override
    public void onDeleteClicked(FrozenItem itemToDelete, int position) {
        archiveItem(itemToDelete, position);
    }

    @Override
    public void onTakeButtonClicked(FrozenItem item) {

    }
}
