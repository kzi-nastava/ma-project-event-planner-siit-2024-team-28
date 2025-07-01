package com.eventplanner.fragments.auth;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.eventplanner.R;

public class RegistrationFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registration, container, false);

        Button btnOrganizer = view.findViewById(R.id.btnOrganizer);
        Button btnBusiness = view.findViewById(R.id.btnBusiness);

        Activity activity = getActivity();
        if (activity != null) {
            NavController navController = Navigation.findNavController(getActivity(), R.id.fragment_nav_content_main);

            btnOrganizer.setOnClickListener(v -> {
                navController.navigate(R.id.nav_event_organizer_registration);
            });

            btnBusiness.setOnClickListener(v -> {
                navController.navigate(R.id.nav_business_owner_registration);
            });
        }

        return view;
    }
}
