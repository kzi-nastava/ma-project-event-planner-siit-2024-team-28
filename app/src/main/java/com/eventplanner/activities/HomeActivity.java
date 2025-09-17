package com.eventplanner.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

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
import com.eventplanner.model.constants.UserRoles;
import com.eventplanner.utils.AuthUtils;
import com.eventplanner.utils.HttpUtils;
import com.eventplanner.utils.WebSocketService;
import com.google.android.material.navigation.NavigationView;

import org.osmdroid.config.Configuration;

import java.io.File;

public class HomeActivity extends AppCompatActivity {
    private NavController navController;
    private AppBarConfiguration mAppBarConfiguration;
    private static final int REQUEST_INTERNET_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check for Internet permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, REQUEST_INTERNET_PERMISSION);
        } else {
            initializeApp();
        }

        // Lazily gets the singleton instance and establishes the WebSocket/STOMP connection
        // Logs once the connection is successfully opened
        WebSocketService.getInstance().connect(() -> {
            Log.d("App", "WebSocket connected!");
        });
    }

    private void initializeApp() {
        EdgeToEdge.enable(this);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_home);

        HttpUtils.initialize(getApplicationContext());
        setupNavigation();
        setupWindowInsets();

        Configuration.getInstance().setUserAgentValue(getPackageName());
        Configuration.getInstance().setOsmdroidBasePath(new File(getCacheDir(), "osmdroid"));
        Configuration.getInstance().setOsmdroidTileCache(new File(getCacheDir(), "osmdroid/tiles"));
    }

    private void setupNavigation() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        navController = Navigation.findNavController(this, R.id.fragment_nav_content_main);

        // Set up navigation drawer
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home,
                R.id.nav_top_events,
                R.id.nav_top_solutions,
                R.id.nav_profile)
                .setOpenableLayout(drawer)
                .build();

        NavigationUI.setupWithNavController(navigationView, navController);

        // Handle custom navigation items
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_logout) {
                handleLogout();
                drawer.closeDrawers();
                return true;
            }

            // Let NavigationUI handle other items
            boolean handled = NavigationUI.onNavDestinationSelected(item, navController);
            if (handled) {
                drawer.closeDrawers();
            }
            return handled;
        });

        updateNavMenu();
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void handleLogout() {
        AuthUtils.clearToken(this);
        updateNavMenu();
        navController.navigate(R.id.nav_home);
    }

    public void updateNavMenu() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();

        boolean loggedIn = AuthUtils.getToken(this) != null;

        // Auth-related items
        menu.findItem(R.id.nav_login).setVisible(!loggedIn);
        menu.findItem(R.id.nav_registration).setVisible(!loggedIn);
        menu.findItem(R.id.nav_logout).setVisible(loggedIn);
        menu.findItem(R.id.nav_profile).setVisible(loggedIn);

        // Admin-only items
        boolean isAdmin = loggedIn && AuthUtils.getUserRoles(this).contains(UserRoles.ADMIN);
        menu.findItem(R.id.nav_event_types).setVisible(isAdmin);
        menu.findItem(R.id.nav_categories_overview).setVisible(isAdmin);

        // Business owner-only items
        boolean isBusinessOwner = loggedIn && AuthUtils.getUserRoles(this).contains(UserRoles.BusinessOwner);
        menu.findItem(R.id.nav_service_overview).setVisible(isBusinessOwner);
        menu.findItem(R.id.nav_my_products).setVisible(isBusinessOwner);
        menu.findItem(R.id.nav_price_list).setVisible(isBusinessOwner);

        // All products should be visible to everyone
        menu.findItem(R.id.nav_all_products).setVisible(true);

        navigationView.invalidate();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return NavigationUI.onNavDestinationSelected(item, navController) ||
                super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) ||
                super.onSupportNavigateUp();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_INTERNET_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeApp();
            } else {
                finish();
            }
        }
    }
}