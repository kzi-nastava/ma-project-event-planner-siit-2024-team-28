package com.eventplanner.fragments.registration;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.eventplanner.R;

public class RegistrationFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registration, container, false);

        Button btnOrganizer = view.findViewById(R.id.btnOrganizer);
        Button btnBusiness = view.findViewById(R.id.btnBusiness);

        btnOrganizer.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), EventOrganizerRegistrationFragment.class);
            startActivity(intent);
        });

        btnBusiness.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), BusinessOwnerRegistrationFragment.class);
            startActivity(intent);
        });

        return view;
    }
}
