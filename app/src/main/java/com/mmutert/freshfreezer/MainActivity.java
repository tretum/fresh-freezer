package com.mmutert.freshfreezer;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.mmutert.freshfreezer.util.Keyboard;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import static com.mmutert.freshfreezer.notification.NotificationConstants.CHANNEL_ID;


public class MainActivity extends AppCompatActivity {


    private AppBarConfiguration mAppBarConfiguration;
    private NavigationView mNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        getWindow().setWindowAnimations(R.style.WindowAnimationTransition);

        setContentView(R.layout.activity_main);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        mNavigationView = findViewById(R.id.nav_view);
        Menu menu = mNavigationView.getMenu();

        // TODO Remove test for programatic addition of entries
        MenuItem item = menu.findItem(R.id.nav_drawer_group_categories_title);
        SubMenu categoriesMenu = item.getSubMenu();
        categoriesMenu.add(R.id.nav_drawer_group_categories, View.generateViewId(), Menu.NONE, "Chilled");
        categoriesMenu.add(R.id.nav_drawer_group_categories, View.generateViewId(), Menu.NONE, "Fruit");
        categoriesMenu.add(R.id.nav_drawer_group_categories, View.generateViewId(), Menu.NONE, "Vegetable");

        mAppBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph())
                .setOpenableLayout(drawer)
                .build();
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(
//                mNavigationView, navController);
                toolbar, navController, mAppBarConfiguration);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (getCurrentFocus() != null) {
                Keyboard.hideKeyboardFrom(this, getCurrentFocus());
            }
        });

        mNavigationView.setNavigationItemSelectedListener(menuItem -> {
            @IdRes
            int id = menuItem.getItemId();
            NavOptions.Builder optionsBuilder = new NavOptions.Builder();

            optionsBuilder
                    .setEnterAnim(R.anim.slide_in_right)
                    .setExitAnim(R.anim.slide_out_left)
                    .setPopEnterAnim(R.anim.slide_in_left)
                    .setPopExitAnim(R.anim.slide_out_right);
            switch (id) {
                case R.id.aboutFragment:
                case R.id.settingsFragment:
                    navController.navigate(id, null, optionsBuilder.build());
                    break;
            }

            // Do not forget to close the drawer
             drawer.closeDrawers();
            return true;
        });

        // Create the notification channel for the app on android versions above O
        createNotificationChannel();
    }


    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.notification_channel_title);
            String description = getString(R.string.notification_channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    @Override
    public boolean onSupportNavigateUp() {

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
