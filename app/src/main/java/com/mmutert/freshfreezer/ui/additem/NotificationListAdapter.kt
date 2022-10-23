package com.mmutert.freshfreezer.ui.additem

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mmutert.freshfreezer.R
import com.mmutert.freshfreezer.data.ItemNotification
import com.mmutert.freshfreezer.data.TimeOffsetUnit
import com.mmutert.freshfreezer.databinding.ItemNotificationEntryBinding
import com.mmutert.freshfreezer.ui.additem.NotificationListAdapter.NotificationListAdapterViewHolder
import java.util.*

/**
 * The Adapter for the list of pending or created notifications.
 */
class NotificationListAdapter(
    private val context: Context,
    private val viewModel: AddItemViewModel
) : ListAdapter<ItemNotification, NotificationListAdapterViewHolder>(DiffCallBack()) {

    fun setItems(notificationList: List<ItemNotification>) {
        submitList(ArrayList(notificationList))
    }

    fun addNotificationEntry(notification: ItemNotification) {
        Log.d(AddItemFragment.LOG_TAG, "Adding notification entry to RV")
        val notifications = ArrayList(currentList)
        notifications.add(notification)
        submitList(notifications)
    }

    private fun removeNotificationEntry(notification: ItemNotification) {
        Log.d(AddItemFragment.LOG_TAG, "Removing notification entry from RV")
        val notifications = ArrayList(currentList)
        notifications.remove(notification)
        submitList(notifications)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NotificationListAdapterViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemNotificationEntryBinding.inflate(layoutInflater, parent, false)
        return NotificationListAdapterViewHolder(binding)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: NotificationListAdapterViewHolder, position: Int) {
        val notification = getNotificationAtPosition(position)
        holder.bind(notification)
    }

    private fun getNotificationAtPosition(position: Int): ItemNotification {
        return currentList[position]
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    /**
     * The view holder for the notification list recycler view
     */
    inner class NotificationListAdapterViewHolder(val binding: ItemNotificationEntryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: ItemNotification) {
            val timeOffsetUnit = notification.timeOffsetUnit
            val offsetAmount = notification.offsetAmount
            when (timeOffsetUnit) {
                TimeOffsetUnit.DAYS -> binding.tvNotificationEntry.text =
                    context.resources.getQuantityString(
                        R.plurals.notification_list_entry_days_before_capitalized,
                        offsetAmount,
                        offsetAmount
                    )
                TimeOffsetUnit.WEEKS -> binding.tvNotificationEntry.text =
                    context.resources.getQuantityString(
                        R.plurals.notification_list_entry_weeks_before_capitalized,
                        offsetAmount,
                        offsetAmount
                    )
                TimeOffsetUnit.MONTHS -> binding.tvNotificationEntry.text =
                    context.resources.getQuantityString(
                        R.plurals.notification_list_entry_months_before_capitalized,
                        offsetAmount,
                        offsetAmount
                    )
            }

            // Add the delete button for the notification in the list
            binding.tvNotificationEntry.setOnTouchListener { v1: View?, event: MotionEvent ->
                val DRAWABLE_LEFT = 0
                val DRAWABLE_TOP = 1
                val DRAWABLE_RIGHT = 2
                val DRAWABLE_BOTTOM = 3
                if (event.action == MotionEvent.ACTION_DOWN) {
                    val rightDrawablePositionX = binding.tvNotificationEntry.right
                    val rightDrawableWidth =
                        binding.tvNotificationEntry.compoundDrawables[DRAWABLE_RIGHT]
                            .bounds
                            .width()
                    if (event.rawX >= rightDrawablePositionX - rightDrawableWidth) {
                        // Remove notification from list
                        viewModel.addNotificationToDelete(notification)
                        removeNotificationEntry(notification)
                        return@setOnTouchListener true
                    }
                }
                v1?.performClick()
                false
            }
        }
    }


    class DiffCallBack : DiffUtil.ItemCallback<ItemNotification>() {
        override fun areItemsTheSame(
            oldNotification: ItemNotification,
            newNotification: ItemNotification
        ): Boolean {
            // Notification properties may have changed if reloaded from the DB, but ID is fixed
            // TODO Fix
            return oldNotification.offsetAmount == newNotification.offsetAmount
                    && (oldNotification.timeOffsetUnit == newNotification.timeOffsetUnit)
        }

        override fun areContentsTheSame(
            oldNotification: ItemNotification,
            newNotification: ItemNotification
        ): Boolean {
            // NOTE: if you use equals, your object must properly override Object#equals()
            // Incorrectly returning false here will result in too many animations.
            return oldNotification == newNotification
        }
    }
}