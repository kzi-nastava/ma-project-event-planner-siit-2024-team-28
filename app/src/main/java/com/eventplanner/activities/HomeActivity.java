package com.eventplanner.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.android.material.navigation.NavigationView;

public class HomeActivity extends AppCompatActivity {
    private NavController navController;
    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

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

            if (id == R.id.nav_menu_register) {
                navController.navigate(R.id.nav_registration);
            } else if (id == R.id.nav_menu_login) {
                navController.navigate(R.id.nav_registration);
            } else if (id == R.id.nav_menu_create_service) {
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
}