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

import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class LocationDialogFragment extends DialogFragment {
    private FragmentLocationDialogBinding binding;
    private CreateLocationRequest location;
    private boolean isReadOnly;

    private MapView mapView;
    private Marker centerMarker;
    private double currentLat = 44.8125; // Default Belgrade
    private double currentLng = 20.4612;

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
        binding.dialogTitle.setText(getString(com.eventplanner.R.string.event_location));

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

        mapView = binding.locationMap;
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

// Restore coordinates if location passed
        if (location != null) {
            currentLat = location.getLatitude();
            currentLng = location.getLongitude();
        }

// Center map
        GeoPoint startPoint = new GeoPoint(currentLat, currentLng);
        mapView.getController().setZoom(13.0);
        mapView.getController().setCenter(startPoint);

// Add marker at center
        centerMarker = new Marker(mapView);
        centerMarker.setPosition(startPoint);
        if(!isReadOnly)
        {
            centerMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            // Listen to map movements
            mapView.addMapListener(new MapListener() {
                @Override
                public boolean onScroll(ScrollEvent event) {
                    updateCenterMarker();
                    return true;
                }

                @Override
                public boolean onZoom(ZoomEvent event) {
                    updateCenterMarker();
                    return true;
                }
            });


        }

        centerMarker.setTitle(location != null ? location.getName() : "Location");
        mapView.getOverlays().add(centerMarker);

// Update lat/lon fields initially
        binding.latitude.setText(String.valueOf(currentLat));
        binding.longitude.setText(String.valueOf(currentLng));


    }
    private void updateCenterMarker() {
        GeoPoint center = (GeoPoint) mapView.getMapCenter();
        currentLat = center.getLatitude();
        currentLng = center.getLongitude();

        centerMarker.setPosition(center);

        binding.latitude.setText(String.format("%.6f", currentLat));
        binding.longitude.setText(String.format("%.6f", currentLng));
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume(); // needed for osmdroid
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause(); // needed for osmdroid
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
            Toast.makeText(requireContext(), getString(com.eventplanner.R.string.name_is_required), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (binding.address.getText().toString().isEmpty()) {
            Toast.makeText(requireContext(), getString(com.eventplanner.R.string.address_is_required), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (binding.latitude.getText().toString().isEmpty()) {
            Toast.makeText(requireContext(), getString(com.eventplanner.R.string.latitude_is_required), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (binding.longitude.getText().toString().isEmpty()) {
            Toast.makeText(requireContext(), getString(com.eventplanner.R.string.longitude_is_required), Toast.LENGTH_SHORT).show();
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