package com.mmutert.freshfreezer.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.WorkManager;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.mmutert.freshfreezer.R;
import com.mmutert.freshfreezer.data.FrozenItem;
import com.mmutert.freshfreezer.data.ItemNotification;
import com.mmutert.freshfreezer.databinding.FragmentFrozenItemListBinding;
import com.mmutert.freshfreezer.notification.NotificationConstants;
import com.mmutert.freshfreezer.notification.NotificationHelper;
import com.mmutert.freshfreezer.viewmodel.FrozenItemViewModel;

import org.joda.time.LocalDateTime;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class FrozenItemListFragment extends Fragment
        implements ListItemClickedCallback,
        ListItemDeleteClickedCallback,
        ListItemTakeClickedCallback,
        TakeOutDialogFragment.TakeOutDialogClickListener {

    private FragmentFrozenItemListBinding mBinding;
    private FrozenItemViewModel mViewModel;
    private ItemListAdapter mItemListAdapter;


    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        mBinding = FragmentFrozenItemListBinding.inflate(inflater, container, false);

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // RecyclerView setup
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(view.getContext(), LinearLayoutManager.VERTICAL, false);
        mBinding.rvFrozenItemList.setLayoutManager(layoutManager);

        mViewModel = new ViewModelProvider(this).get(FrozenItemViewModel.class);

        mItemListAdapter = new ItemListAdapter(mViewModel, this, this);

        // Observe the items in the database that have to get added to the database
        mViewModel.getFrozenItems().observe(getViewLifecycleOwner(), mItemListAdapter::setItems);


        mBinding.rvFrozenItemList.setAdapter(mItemListAdapter);

        ItemTouchHelper itemTouchHelper = createSwipeHelper();
        itemTouchHelper.attachToRecyclerView(mBinding.rvFrozenItemList);


        mBinding.fab.setOnClickListener(view2 -> {
            mBinding.fab.setVisibility(View.GONE);
            Navigation.findNavController(view).navigate(R.id.action_item_list_to_add_item);
            Log.d("", "Clicked FAB");
        });
    }

    /**
     * Creates the ItemTouchHelper that archives items in the item list on swipe to the right.
     * @return The item touch helper
     */
    private ItemTouchHelper createSwipeHelper() {
        return new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
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
                archiveItem(mItemListAdapter.getItemAtPosition(pos));
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        Log.d(FrozenItemListFragment.class.getCanonicalName(), "Creating list options menu");
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_item_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
//            NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
//                    .findFragmentById(R.id.nav_host_fragment);
//            navHostFragment.getNavController().navigate(R.id.action_settings);
            return true;
        } else if (id == R.id.app_bar_filter) {
            ListSortingDialogFragment listSortingDialogFragment = new ListSortingDialogFragment(
                    getContext(),
                    mViewModel.getSortingOption(),
                    mViewModel.getSortingOrder(),
                    mItemListAdapter
            );
            listSortingDialogFragment.show(getParentFragmentManager(), "set sorting option");
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(FrozenItem item) {
        Log.d("ListFragment", "Clicked on item " + item.getName());
    }


    private void archiveItem(FrozenItem itemToArchive) {

        FrozenItemViewModel viewModel = new ViewModelProvider(this).get(FrozenItemViewModel.class);
        Snackbar snackbar = Snackbar.make(
                mBinding.itemListCoordinatorLayout,
                "Deleted item " + itemToArchive.getName(),
                Snackbar.LENGTH_LONG
        );

        // TODO Warning: Hack, better change this
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<ItemNotification> allNotifications = viewModel.getAllNotifications(itemToArchive);

            // Cancel all notifications
            WorkManager workManager = WorkManager.getInstance(getContext());
            for (ItemNotification notification : allNotifications) {
                workManager.cancelWorkById(notification.getNotificationId());
            }

            snackbar.setAction("Undo", v -> {
                viewModel.restore(itemToArchive);

                // TODO Create new workers for notifications
                for (ItemNotification notification : allNotifications) {
                    LocalDateTime notifyOn = notification.getNotifyOn();
                    UUID uuid = NotificationHelper.scheduleNotification(
                            getContext(),
                            itemToArchive,
                            NotificationConstants.NOTIFICATION_OFFSET_TIMEUNIT,
                            notifyOn
                    );
                    viewModel.addNotification(itemToArchive, uuid, notifyOn);
                    viewModel.deleteNotification(notification);
                }
            });
            snackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE);
            snackbar.show();
            viewModel.archive(itemToArchive);
        });
    }

    @Override
    public void onDeleteClicked(FrozenItem itemToDelete, int position) {
        archiveItem(itemToDelete);
    }

    @Override
    public void onTakeButtonClicked(FrozenItem item) {
        new TakeOutDialogFragment(this, item).show(getParentFragmentManager(), "take out");
    }

    @Override
    public void onPositiveClick(final TakeOutDialogFragment dialog) {
        // Update the item from which the things were taken
        FrozenItemViewModel viewModel = new ViewModelProvider(this).get(FrozenItemViewModel.class);
        float amount = dialog.getSelectionAmount();
        FrozenItem item = dialog.getItem();
        viewModel.updateItem(item, amount);

        // TODO Possibly a hack. The amount was not updated because the current item is changed in the view model.
        //  Therefore the DiffUtil does not recognize the item as changed and the recycler view will not be notified of changes.
        //  The same applies for the neutral click
        mItemListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onNeutralClick(final TakeOutDialogFragment dialog) {
        FrozenItemViewModel viewModel = new ViewModelProvider(this).get(FrozenItemViewModel.class);
        viewModel.updateItem(dialog.getItem(), dialog.getItem().getAmount());
        mItemListAdapter.notifyDataSetChanged();
    }

}
