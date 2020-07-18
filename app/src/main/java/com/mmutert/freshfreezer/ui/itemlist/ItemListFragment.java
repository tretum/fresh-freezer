package com.mmutert.freshfreezer.ui.itemlist;

import android.content.SharedPreferences;
import android.graphics.Canvas;
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
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.mmutert.freshfreezer.R;
import com.mmutert.freshfreezer.data.FrozenItem;
import com.mmutert.freshfreezer.databinding.FragmentFrozenItemListBinding;
import com.mmutert.freshfreezer.databinding.ListItemBinding;
import com.mmutert.freshfreezer.ui.ListSortingDialogFragment;
import com.mmutert.freshfreezer.ui.TakeOutDialogFragment;
import com.mmutert.freshfreezer.viewmodel.ItemListViewModel;


/**
 *
 */
public class ItemListFragment extends Fragment implements ListItemClickedCallback {

    private FragmentFrozenItemListBinding mBinding;
    private ItemListViewModel mViewModel;
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

        boolean darkModeEnabled = getDarkModeEnabledPreference();
        applyDarkMode(darkModeEnabled);

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // RecyclerView setup
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(view.getContext(), LinearLayoutManager.VERTICAL, false);
        mBinding.rvFrozenItemList.setLayoutManager(layoutManager);

        mViewModel = new ViewModelProvider(this).get(ItemListViewModel.class);

        mItemListAdapter = new ItemListAdapter(mViewModel, this, getContext());

        // Observe the items in the database that have to get added to the database
        mViewModel.getFrozenItems().observe(getViewLifecycleOwner(), mItemListAdapter::setItems);


        mBinding.rvFrozenItemList.setAdapter(mItemListAdapter);

        ItemTouchHelper itemTouchHelper = createSwipeHelper();
        itemTouchHelper.attachToRecyclerView(mBinding.rvFrozenItemList);

        mBinding.rvFrozenItemList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    mBinding.fab.hide();
                } else {
                    mBinding.fab.show();
                }

                super.onScrolled(recyclerView, dx, dy);
            }
        });


        setupNewItemFAB();
    }

    private void setupNewItemFAB() {
        mBinding.fab.setOnClickListener(view2 -> {
            // TODO Check if setting to invisible is still necessary
            mBinding.fab.setVisibility(View.GONE);

            FrozenItemListFragmentDirections.ActionOpenAddItemView navDirections
                    = FrozenItemListFragmentDirections.actionOpenAddItemView();
            navDirections.setItemId(-1);
            Navigation.findNavController(view2).navigate(navDirections);
            Log.d("", "Clicked FAB");
        });
    }

    /**
     * Creates the ItemTouchHelper that archives items in the item list on swipe to the right.
     *
     * @return The item touch helper
     */
    private ItemTouchHelper createSwipeHelper() {
        return new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT
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
                FrozenItem item = mItemListAdapter.getItemAtPosition(pos);
                if (direction == ItemTouchHelper.RIGHT) {
                    archiveItem(item);
                } else if (direction == ItemTouchHelper.LEFT) {
                    new TakeOutDialogFragment(new TakeListener(), item).show(getParentFragmentManager(), "take out");
                    mItemListAdapter.notifyItemChanged(pos);
                }
            }

            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {

                if (viewHolder != null) {
                    ListItemBinding binding = ((ItemListAdapter.ItemListAdapterViewHolder) viewHolder).binding;
                    final View foregroundView = binding.listItemForeground;

                    getDefaultUIUtil().onSelected(foregroundView);
                }
            }

            @Override
            public void onChildDrawOver(
                    @NonNull Canvas c, @NonNull RecyclerView recyclerView,
                    RecyclerView.ViewHolder viewHolder, float dX, float dY,
                    int actionState, boolean isCurrentlyActive) {

                ListItemBinding binding = ((ItemListAdapter.ItemListAdapterViewHolder) viewHolder).binding;
                View deleteBackground = binding.listItemDeleteBackground;
                View takeBackground = binding.listItemTakeBackground;

                if (dX < 0) {
                    deleteBackground.setVisibility(View.INVISIBLE);
                    takeBackground.setVisibility(View.VISIBLE);
                } else if (dX > 0) {
                    deleteBackground.setVisibility(View.VISIBLE);
                    takeBackground.setVisibility(View.INVISIBLE);
                } else {
                    deleteBackground.setVisibility(View.INVISIBLE);
                    takeBackground.setVisibility(View.INVISIBLE);
                }

                final View foregroundView = binding.listItemForeground;
                getDefaultUIUtil().onDrawOver(c, recyclerView, foregroundView, dX, dY, actionState, isCurrentlyActive);
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {

                ListItemBinding binding = ((ItemListAdapter.ItemListAdapterViewHolder) viewHolder).binding;
                final View foregroundView = binding.listItemForeground;
                getDefaultUIUtil().clearView(foregroundView);
            }

            @Override
            public void onChildDraw(
                    @NonNull Canvas c, @NonNull RecyclerView recyclerView,
                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                    int actionState, boolean isCurrentlyActive) {

                ListItemBinding binding = ((ItemListAdapter.ItemListAdapterViewHolder) viewHolder).binding;
                final View foregroundView = binding.listItemForeground;

                getDefaultUIUtil().onDraw(c, recyclerView, foregroundView, dX, dY, actionState, isCurrentlyActive);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        Log.d(ItemListFragment.class.getCanonicalName(), "Creating list options menu");
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_item_list, menu);

        boolean darkModeEnabled = getDarkModeEnabledPreference();
        MenuItem item = menu.findItem(R.id.action_toggle_dark_mode);
        item.setChecked(darkModeEnabled);
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
        } else if (id == R.id.action_toggle_dark_mode) {
            if (item.isChecked()) {
                saveDarkModePreference(false);
                applyDarkMode(false);
                item.setChecked(false);
            } else {
                saveDarkModePreference(true);
                applyDarkMode(true);
                item.setChecked(true);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Returns the saved value for the preference of dark mode.
     *
     * @return The saved value.
     */
    private boolean getDarkModeEnabledPreference() {
        SharedPreferences defaultSharedPreferences
                = PreferenceManager.getDefaultSharedPreferences(getContext());
        return defaultSharedPreferences.getBoolean("darkMode", false);
    }

    private void saveDarkModePreference(boolean enabled) {
        SharedPreferences defaultSharedPreferences
                = PreferenceManager.getDefaultSharedPreferences(getContext());
        defaultSharedPreferences.edit().putBoolean("darkMode", enabled).apply();
    }

    /**
     * Applies the given status to dark mode
     *
     * @param enabled
     */
    private void applyDarkMode(boolean enabled) {
        if (enabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }

    @Override
    public void onClick(FrozenItem item) {
        Log.d("ListFragment", "Clicked on item " + item.getName());
        FrozenItemListFragmentDirections.ActionOpenAddItemView navDirections
                = FrozenItemListFragmentDirections.actionOpenAddItemView();
        navDirections.setItemId(item.getId());
        Navigation.findNavController(mBinding.getRoot()).navigate(navDirections);
    }


    /**
     * Archives the given item and displays a snackbar that allows undoing the operation.
     *
     * @param itemToArchive The item to archive.
     */
    private void archiveItem(FrozenItem itemToArchive) {

        /**
         * The snackbar that is displayed when an item is deleted in order to allow undoing the action.
         */
        Snackbar mDeleteSnackbar = Snackbar.make(
                mBinding.itemListCoordinatorLayout,
                "Deleted item " + itemToArchive.getName(),
                Snackbar.LENGTH_LONG
        );
        mDeleteSnackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE);

        // Actually archive the item
        // This causes the list to be updated and the RV to be updated.
        // We do not cancel the scheduled notifications here and only do that only if the action was not undone.
        mViewModel.archive(itemToArchive);

        // Undoing the action restores the item from the archive and the RV will be updated automatically
        // Scheduling the notifications is not required since they were not cancelled until undo is no longer possible
        mDeleteSnackbar.setAction("Undo", v -> {
            mViewModel.restore(itemToArchive);
        });

        // Adds a callback that finally actually archives the item when the snackbar times out
        mDeleteSnackbar.addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
            @Override
            public void onDismissed(final Snackbar transientBottomBar, final int event) {
                if (event == DISMISS_EVENT_TIMEOUT
                        || event == DISMISS_EVENT_CONSECUTIVE
                        || event == DISMISS_EVENT_SWIPE
                        || event == DISMISS_EVENT_MANUAL) {
                    mViewModel.cancelNotifications(itemToArchive);

                }
                super.onDismissed(transientBottomBar, event);
            }
        });
        mDeleteSnackbar.show();
    }

    /**
     * For the given item take the specified amount and use the view model to update the item in the database.
     *
     * @param item        The item where the amount was taken from
     * @param amountTaken The amount that was taken from the item
     */
    private void takeFromItem(FrozenItem item, float amountTaken) {
        float newAmount = Math.max(0.0F, item.getAmount() - amountTaken);
        mViewModel.takeFromItem(item, amountTaken);

        // TODO Possibly a hack. The amount was not updated because the current item is changed in the view model.
        //  Therefore the DiffUtil does not recognize the item as changed and the recycler view will not be notified of changes.
        mItemListAdapter.notifyDataSetChanged();
    }

    private class TakeListener implements TakeOutDialogFragment.TakeOutDialogClickListener {

        @Override
        public void onPositiveClicked(final TakeOutDialogFragment dialog) {
            takeFromItem(dialog.getItem(), dialog.getSelectionAmount());
        }

        @Override
        public void onTakeAllClicked(final TakeOutDialogFragment dialog) {
            takeFromItem(dialog.getItem(), dialog.getItem().getAmount());
        }

        @Override
        public void onCancelClicked(final TakeOutDialogFragment dialog) {
            mItemListAdapter.notifyDataSetChanged();
        }
    }

}
