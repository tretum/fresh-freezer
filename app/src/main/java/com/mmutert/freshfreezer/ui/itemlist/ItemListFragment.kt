package com.mmutert.freshfreezer.ui.itemlist

import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.BaseTransientBottomBar.BaseCallback
import com.google.android.material.snackbar.Snackbar
import com.mmutert.freshfreezer.MainActivity
import com.mmutert.freshfreezer.R
import com.mmutert.freshfreezer.data.Condition
import com.mmutert.freshfreezer.data.FrozenItem
import com.mmutert.freshfreezer.databinding.FragmentFrozenItemListBinding
import com.mmutert.freshfreezer.ui.dialogs.ListSortingDialogFragment
import com.mmutert.freshfreezer.ui.dialogs.TakeOutDialogFragment
import com.mmutert.freshfreezer.ui.dialogs.TakeOutDialogFragment.TakeOutDialogClickListener
import com.mmutert.freshfreezer.ui.itemlist.ItemListAdapter.ItemListAdapterViewHolder

/**
 *
 */
class ItemListFragment : Fragment(), ListItemClickedCallback {
    private lateinit var mBinding: FragmentFrozenItemListBinding
    private lateinit var mViewModel: ItemListViewModel
    private lateinit var mItemListAdapter: ItemListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mBinding = FragmentFrozenItemListBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewModel = ViewModelProvider(this).get(ItemListViewModel::class.java)

        // RecyclerView setup
        val conditionArg = this.requireArguments().getInt("condition", MainActivity.NO_FILTER_ID)
//        ItemListFragmentArgs itemListFragmentArgs = ItemListFragmentArgs.fromBundle(arguments);
//        if (itemListFragmentArgs.getCondition() != null && !itemListFragmentArgs.getCondition().isEmpty()) {
        if (conditionArg != MainActivity.NO_FILTER_ID) {
            val requestedCondition: Condition = when (conditionArg) {
                1 -> Condition.FROZEN
                2 -> Condition.CHILLED
                3 -> Condition.ROOM_TEMP
                else -> Condition.FROZEN
            }
//            requestedCondition = Condition.valueOf(itemListFragmentArgs.getCondition());
            mViewModel.filterItems(listOf(requestedCondition))
        }
        mItemListAdapter = ItemListAdapter(mViewModel, this, requireContext())

        // Observe the items in the database that have to get added to the database
        mViewModel.frozenItems.observe(
            viewLifecycleOwner,
            { list -> mItemListAdapter.itemList = list })

        mBinding.rvFrozenItemList.apply {

            this.layoutManager =
                    LinearLayoutManager(view.context, LinearLayoutManager.VERTICAL, false)
            adapter = mItemListAdapter
            val itemTouchHelper = createSwipeHelper()
            itemTouchHelper.attachToRecyclerView(this)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy > 0) {
                        mBinding.fab.hide()
                    } else {
                        mBinding.fab.show()
                    }
                    super.onScrolled(recyclerView, dx, dy)
                }

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    when(newState){
                        RecyclerView.SCROLL_STATE_IDLE, RecyclerView.SCROLL_STATE_SETTLING ->
                            mBinding.fab.show()
                    }
                }
            })

            addItemDecoration(BottomSpaceDecoration(200))
        }

        setupNewItemFAB()
    }

    private fun setupNewItemFAB() {
        mBinding.fab.setOnClickListener { view2: View? ->
            val title = getString(R.string.fragment_add_item_label)
            val navDirections = ItemListFragmentDirections.actionOpenAddItemView(title)
            navDirections.itemId = -1
            Navigation.findNavController(view2!!).navigate(navDirections)
            Log.d("", "Clicked FAB")
        }
    }

    /**
     * Creates the ItemTouchHelper that archives items in the item list on swipe to the right.
     *
     * @return The item touch helper
     */
    private fun createSwipeHelper(): ItemTouchHelper {
        return ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT
        ) {
            override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Delete the item
                val pos = viewHolder.adapterPosition
                val item = mItemListAdapter.getItemAtPosition(pos)
                if (direction == ItemTouchHelper.RIGHT) {
                    archiveItem(item)
                } else if (direction == ItemTouchHelper.LEFT) {
                    TakeOutDialogFragment(TakeListener(), item).show(
                        parentFragmentManager,
                        "take out")
                    mItemListAdapter.notifyItemChanged(pos)
                }
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                if (viewHolder != null) {
                    val binding = (viewHolder as ItemListAdapterViewHolder).binding
                    val foregroundView: View = binding.listItemForeground
                    getDefaultUIUtil().onSelected(foregroundView)
                }
            }

            override fun onChildDrawOver(
                    c: Canvas, recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float,
                    actionState: Int, isCurrentlyActive: Boolean) {
                val binding = (viewHolder as ItemListAdapterViewHolder).binding
                val deleteBackground: View = binding.listItemDeleteBackground
                val takeBackground: View = binding.listItemTakeBackground
                when {
                    dX < 0 -> {
                        deleteBackground.visibility = View.INVISIBLE
                        takeBackground.visibility = View.VISIBLE
                    }
                    dX > 0 -> {
                        deleteBackground.visibility = View.VISIBLE
                        takeBackground.visibility = View.INVISIBLE
                    }
                    else   -> {
                        deleteBackground.visibility = View.INVISIBLE
                        takeBackground.visibility = View.INVISIBLE
                    }
                }
                val foregroundView: View = binding.listItemForeground
                getDefaultUIUtil().onDrawOver(
                    c,
                    recyclerView,
                    foregroundView,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive)
            }

            override fun clearView(recyclerView: RecyclerView,
                                   viewHolder: RecyclerView.ViewHolder) {
                val binding = (viewHolder as ItemListAdapterViewHolder).binding
                val foregroundView: View = binding.listItemForeground
                getDefaultUIUtil().clearView(foregroundView)
            }

            override fun onChildDraw(
                    c: Canvas, recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float,
                    actionState: Int, isCurrentlyActive: Boolean) {
                val binding = (viewHolder as ItemListAdapterViewHolder).binding
                val foregroundView: View = binding.listItemForeground
                getDefaultUIUtil().onDraw(
                    c,
                    recyclerView,
                    foregroundView,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        Log.d(ItemListFragment::class.java.canonicalName, "Creating list options menu")
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.menu_item_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        if (id == R.id.app_bar_filter) {
            val listSortingDialogFragment = ListSortingDialogFragment(
                mViewModel.sortingOption,
                mViewModel.sortingOrder,
                mItemListAdapter
            )
            listSortingDialogFragment.show(parentFragmentManager, "set sorting option")
            return true
        }
        return false
    }

    override fun onClick(item: FrozenItem) {
        Log.d("ListFragment", "Clicked on item " + item.name)
        val title = getString(R.string.add_item_label_editing)
        val navDirections = ItemListFragmentDirections.actionOpenAddItemView(title)
        navDirections.itemId = item.id
        Navigation.findNavController(mBinding.root).navigate(navDirections)
    }

    /**
     * Archives the given item and displays a snackbar that allows undoing the operation.
     *
     * @param itemToArchive The item to archive.
     */
    private fun archiveItem(itemToArchive: FrozenItem) {
        /**
         * The snackbar that is displayed when an item is deleted in order to allow undoing the action.
         */
        val mDeleteSnackbar = Snackbar.make(
            mBinding.itemListCoordinatorLayout,
            "Deleted item " + itemToArchive.name,
            Snackbar.LENGTH_LONG
        )
        mDeleteSnackbar.animationMode = BaseTransientBottomBar.ANIMATION_MODE_SLIDE

        // Actually archive the item
        // This causes the list to be updated and the RV to be updated.
        // We do not cancel the scheduled notifications here and only do that only if the action was not undone.
        mViewModel.archive(itemToArchive)

        // Undoing the action restores the item from the archive and the RV will be updated automatically
        // Scheduling the notifications is not required since they were not cancelled until undo is no longer possible
        mDeleteSnackbar.setAction(getString(R.string.undo_button_label)) {
            mViewModel.restore(itemToArchive)
        }

        // Adds a callback that finally actually archives the item when the snackbar times out
        mDeleteSnackbar.addCallback(object : BaseCallback<Snackbar?>() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                if (event == DISMISS_EVENT_TIMEOUT || event == DISMISS_EVENT_CONSECUTIVE || event == DISMISS_EVENT_SWIPE || event == DISMISS_EVENT_MANUAL) {
                    mViewModel.cancelNotifications(itemToArchive)
                }
                super.onDismissed(transientBottomBar, event)
            }
        })
        mDeleteSnackbar.show()
    }

    /**
     * For the given item take the specified amount and use the view model to update the item in the database.
     *
     * @param item        The item where the amount was taken from
     * @param amountTaken The amount that was taken from the item
     */
    private fun takeFromItem(item: FrozenItem, amountTaken: Float) {
        mViewModel.takeFromItem(item, amountTaken)

        // TODO Possibly a hack. The amount was not updated because the current item is changed in the view model.
        //  Therefore the DiffUtil does not recognize the item as changed and the recycler view will not be notified of changes.
        mItemListAdapter.notifyItemChanged(mItemListAdapter.getPositionOfItem(item))
    }

    private inner class TakeListener : TakeOutDialogClickListener {
        override fun onPositiveClicked(dialog: TakeOutDialogFragment) {
            takeFromItem(dialog.item, dialog.selectionAmount)
        }

        override fun onTakeAllClicked(dialog: TakeOutDialogFragment) {
            takeFromItem(dialog.item, dialog.item.amount)
        }

        override fun onCancelClicked(dialog: TakeOutDialogFragment) {
            mItemListAdapter.notifyDataSetChanged()
        }
    }
}