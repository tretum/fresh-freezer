package com.mmutert.freshfreezer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.mmutert.freshfreezer.notification.NotificationConstants
import com.mmutert.freshfreezer.ui.itemlist.ItemListFragmentDirections
import com.mmutert.freshfreezer.util.Keyboard

class MainActivity : AppCompatActivity() {
    private lateinit var mAppBarConfiguration: AppBarConfiguration
    private lateinit var mNavigationView: NavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set content view after applying the theme
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        mNavigationView = findViewById(R.id.nav_view)
        val menu = mNavigationView.menu

        mAppBarConfiguration =
                AppBarConfiguration.Builder(navController.graph).setOpenableLayout(drawer).build()
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration)
        NavigationUI.setupWithNavController(toolbar, navController, mAppBarConfiguration)

        navController.addOnDestinationChangedListener { controller: NavController?, destination: NavDestination?, arguments: Bundle? ->
            val currentFocusTemp = currentFocus
            if (currentFocusTemp != null) {
                Keyboard.hideKeyboardFrom(this, currentFocusTemp)
            }
        }

        mNavigationView.setNavigationItemSelectedListener { menuItem: MenuItem ->
            @IdRes val id = menuItem.itemId

            val optionsBuilder = NavOptions.Builder()
            optionsBuilder.setEnterAnim(R.anim.slide_in_right)
                .setExitAnim(R.anim.slide_out_left)
                .setPopEnterAnim(R.anim.slide_in_left)
                .setPopExitAnim(R.anim.slide_out_right)

            when (id) {
                R.id.aboutFragment, R.id.settingsFragment -> navController.navigate(
                    id, null, optionsBuilder.build())
                R.id.nav_drawer_all_items -> {
                    val direction =
                            ItemListFragmentDirections.actionItemListFragmentFilter(NO_FILTER_ID)
                                .setTitle(getString(R.string.app_name))
                    navController.navigate(direction)
                }
                R.id.drawer_item_condition_frozen -> {
                    val direction = ItemListFragmentDirections.actionItemListFragmentFilter(1)
                        .setTitle(getString(R.string.frozen_items_title))
                    navController.navigateUp()
                    navController.navigate(direction)
                }
                R.id.drawer_item_condition_chilled -> {
                    val direction = ItemListFragmentDirections.actionItemListFragmentFilter(2)
                        .setTitle(getString(R.string.chilled_items_title))
                    navController.navigate(direction)
                }
                R.id.drawer_item_condition_room_temp -> {
                    val direction = ItemListFragmentDirections.actionItemListFragmentFilter(3)
                        .setTitle(getString(R.string.room_temp_items_title))
                    navController.navigate(direction)
                }
            }

            // Do not forget to close the drawer
            drawer.closeDrawers()
            true
        }

        // Create the notification channel for the app on android versions above O
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = getString(R.string.notification_channel_title)
            val description = getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(NotificationConstants.CHANNEL_ID, name, importance)
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(
                NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        return (NavigationUI.navigateUp(
            navController, mAppBarConfiguration) || super.onSupportNavigateUp())
    }

    companion object {
        private const val TAG = "MainActivity"
        const val NO_FILTER_ID = -1
    }
}