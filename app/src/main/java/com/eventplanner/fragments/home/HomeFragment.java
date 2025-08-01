package com.eventplanner.fragments.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.eventplanner.R;
import com.eventplanner.fragments.events.TopEventsFragment;
import com.eventplanner.fragments.solutions.TopSolutionsFragment;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Load TopEventsFragment by default
        loadFragment(new TopEventsFragment());

        Button topEventsButton = view.findViewById(R.id.top_events_button);
        topEventsButton.setOnClickListener(v ->
                loadFragment(new TopEventsFragment())
        );

        Button topSolutionsButton = view.findViewById(R.id.top_solutions_button);
        topSolutionsButton.setOnClickListener(v ->
                loadFragment(new TopSolutionsFragment())
        );
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_top, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}