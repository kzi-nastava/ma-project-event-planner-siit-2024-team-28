package com.eventplanner.fragments.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.eventplanner.R;
import com.eventplanner.fragments.FragmentTransition;
import com.eventplanner.fragments.events.TopEventsFragment;
import com.eventplanner.fragments.solutions.TopSolutionsFragment;

public class HomeFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Button btnFragment1 = view.findViewById(R.id.top_events_button);
        btnFragment1.setOnClickListener(v ->
                FragmentTransition.to(TopEventsFragment.newInstance(), requireActivity(), false, R.id.fragment_top));

        Button btnFragment2 = view.findViewById(R.id.top_solutions_button);
        btnFragment2.setOnClickListener(v ->
                FragmentTransition.to(TopSolutionsFragment.newInstance(), requireActivity(), false, R.id.fragment_top));

        return view;
    }
}
