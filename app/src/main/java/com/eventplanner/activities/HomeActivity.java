package com.eventplanner.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
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
import com.eventplanner.utils.ClientUtils;
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
        setContentView(R.layout.activity_home);

        ClientUtils.initialize(getApplicationContext());
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

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int id = destination.getId();

            // Avoid unnecessary navigation by checking current destination
            if (id == R.id.nav_registration && controller.getCurrentDestination().getId() != R.id.nav_registration) {
                navController.navigate(R.id.nav_registration);
            } else if (id == R.id.nav_login && controller.getCurrentDestination().getId() != R.id.nav_login) {
                navController.navigate(R.id.nav_registration);
            } else if (id == R.id.nav_service_creation && controller.getCurrentDestination().getId() != R.id.nav_service_creation) {
                navController.navigate(R.id.nav_service_creation);
            }
        });

        mAppBarConfiguration = new AppBarConfiguration
                .Builder(R.id.nav_home)
                .setOpenableLayout(drawer)
                .build();
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