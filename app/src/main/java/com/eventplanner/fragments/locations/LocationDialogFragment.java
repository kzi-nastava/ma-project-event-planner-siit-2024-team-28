package com.eventplanner.fragments.locations;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.eventplanner.databinding.FragmentLocationDialogBinding;
import com.eventplanner.model.requests.locations.CreateLocationRequest;
import com.google.gson.Gson;

public class LocationDialogFragment extends DialogFragment {
    private FragmentLocationDialogBinding binding;
    private CreateLocationRequest location;
    private boolean isReadOnly;

    public static LocationDialogFragment newInstance(CreateLocationRequest location, boolean isReadOnly) {
        LocationDialogFragment fragment = new LocationDialogFragment();
        Bundle args = new Bundle();
        // Serialize using Gson
        Gson gson = new Gson();
        args.putString("location", gson.toJson(location));
        args.putBoolean("isReadOnly", isReadOnly);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // Deserialize using Gson
            Gson gson = new Gson();
            String locationJson = getArguments().getString("location");
            location = gson.fromJson(locationJson, CreateLocationRequest.class);
            isReadOnly = getArguments().getBoolean("isReadOnly");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLocationDialogBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set title
        binding.dialogTitle.setText("Location");

        // Populate form if location exists
        if (location != null) {
            binding.name.setText(location.getName());
            binding.address.setText(location.getAddress());
            binding.latitude.setText(String.valueOf(location.getLatitude()));
            binding.longitude.setText(String.valueOf(location.getLongitude()));
        }

        // Disable form if read-only
        if (isReadOnly) {
            binding.name.setEnabled(false);
            binding.address.setEnabled(false);
            binding.latitude.setEnabled(false);
            binding.longitude.setEnabled(false);
            binding.saveButton.setVisibility(View.GONE);
        }

        // Setup buttons
        binding.cancelButton.setOnClickListener(v -> dismiss());
        binding.saveButton.setOnClickListener(v -> saveLocation());
    }

    private void saveLocation() {
        if (validateForm()) {
            String name = binding.name.getText().toString();
            String address = binding.address.getText().toString();
            double latitude = Double.parseDouble(binding.latitude.getText().toString());
            double longitude = Double.parseDouble(binding.longitude.getText().toString());

            CreateLocationRequest newLocation = new CreateLocationRequest(name, address, latitude, longitude);

            // Pass data back as JSON string
            Bundle result = new Bundle();
            Gson gson = new Gson();
            result.putString("location", gson.toJson(newLocation));
            getParentFragmentManager().setFragmentResult("location_request", result);

            dismiss();
        }
    }

    private boolean validateForm() {
        if (binding.name.getText().toString().isEmpty()) {
            Toast.makeText(requireContext(), "Name is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (binding.address.getText().toString().isEmpty()) {
            Toast.makeText(requireContext(), "Address is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (binding.latitude.getText().toString().isEmpty()) {
            Toast.makeText(requireContext(), "Latitude is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (binding.longitude.getText().toString().isEmpty()) {
            Toast.makeText(requireContext(), "Longitude is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}