package com.eventplanner.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.eventplanner.R;
import com.eventplanner.fragments.FragmentTransition;
import com.eventplanner.fragments.events.TopEventsFragment;
import com.eventplanner.fragments.solutions.TopSolutionsFragment;
import com.eventplanner.model.constants.UserRoles;
import com.eventplanner.utils.AuthUtils;
import com.eventplanner.utils.HttpUtils;
import com.google.android.material.navigation.NavigationView;

public class HomeActivity extends AppCompatActivity {
    private NavController navController;
    private AppBarConfiguration mAppBarConfiguration;
    private static final int REQUEST_INTERNET_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check for Internet permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            // Request permission if not granted
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, REQUEST_INTERNET_PERMISSION);
        } else {
            // Proceed if permission is already granted
            init();
        }
    }

    private void init() {
        EdgeToEdge.enable(this);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_home);

        HttpUtils.initialize(getApplicationContext());
        Button eventsButton = findViewById(R.id.top_events_button);
        eventsButton.setOnClickListener(v -> FragmentTransition.to(TopEventsFragment.newInstance(), HomeActivity.this, false, R.id.fragment_top));
        Button solutionsButton = findViewById(R.id.top_solutions_button);
        solutionsButton.setOnClickListener(v -> FragmentTransition.to(TopSolutionsFragment.newInstance(), HomeActivity.this, false, R.id.fragment_top));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setUpNavBar();
    }

    private void setUpNavBar() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        navController = Navigation.findNavController(this, R.id.fragment_nav_content_main);

        NavigationView navigationView = findViewById(R.id.nav_view);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Handle menu item clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_logout) {
                AuthUtils.clearToken(this);
                updateNavMenu();
                drawer.closeDrawers();
                navController.navigate(R.id.nav_home);
                return true;
            } else {
                // For all other items, let NavigationUI handle navigation
                boolean handled = NavigationUI.onNavDestinationSelected(item, navController);
                if (handled) {
                    drawer.closeDrawers();
                }
                return handled;
            }
        });

        mAppBarConfiguration = new AppBarConfiguration
                .Builder(R.id.nav_home)
                .setOpenableLayout(drawer)
                .build();

        updateNavMenu();
    }

    public void updateNavMenu() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();

        boolean loggedIn = AuthUtils.getToken(this) != null;

        MenuItem loginItem = menu.findItem(R.id.nav_login);
        if (loginItem != null) {
            loginItem.setVisible(!loggedIn);
        }

        MenuItem registerItem = menu.findItem(R.id.nav_registration);
        if (registerItem != null) {
            registerItem.setVisible(!loggedIn);
        }

        MenuItem logoutItem = menu.findItem(R.id.nav_logout);
        if (logoutItem != null) {
            logoutItem.setVisible(loggedIn);
        }

        MenuItem profileItem = menu.findItem(R.id.nav_profile);
        if (profileItem != null) {
            profileItem.setVisible(loggedIn);
        }

        MenuItem eventTypesItem = menu.findItem(R.id.nav_event_types);
        if (eventTypesItem != null) {
            boolean isAdmin = AuthUtils.getUserRoles(this).contains(UserRoles.ADMIN);
            eventTypesItem.setVisible(isAdmin);
        }

        navigationView.invalidate();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        navController = Navigation.findNavController(this, R.id.fragment_nav_content_main);
        return NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        navController = Navigation.findNavController(this, R.id.fragment_nav_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_INTERNET_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with app
                init();
            } else {
                // Permission denied, terminate the app
                finish();
                System.exit(0);
            }
        }
    }
}