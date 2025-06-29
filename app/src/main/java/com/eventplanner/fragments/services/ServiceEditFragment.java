package com.eventplanner.fragments.services;

import android.os.Bundle;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.Toast;

import com.eventplanner.R;
import com.eventplanner.databinding.FragmentServiceCreationBinding;
import com.eventplanner.databinding.FragmentServiceEditBinding;
import com.eventplanner.model.enums.ReservationType;
import com.eventplanner.model.requests.services.CreateServiceRequest;
import com.eventplanner.model.requests.services.UpdateServiceRequest;
import com.eventplanner.model.responses.eventTypes.GetEventTypeResponse;
import com.eventplanner.model.responses.services.GetServiceResponse;
import com.eventplanner.model.responses.solutionCateogries.GetSolutionCategoryResponse;
import com.eventplanner.services.EventTypeService;
import com.eventplanner.services.ServiceService;
import com.eventplanner.utils.AuthUtils;
import com.eventplanner.utils.HttpUtils;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.Collection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServiceEditFragment extends Fragment {
    private static final String ARG_SERVICE_ID = "serviceId";
    private String serviceId;
    private GetServiceResponse service;
    private FragmentServiceEditBinding binding;
    private ServiceService serviceService;
    private EventTypeService eventTypeService;

    public ServiceEditFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        serviceService = HttpUtils.getServiceService();
        eventTypeService = HttpUtils.getEventTypeService();
        if (getArguments() != null) {
            serviceId = getArguments().getString(ARG_SERVICE_ID);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentServiceEditBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Fetch service and then setupViews
        fetchService();

        return view;
    }

    private void fetchService() {
        Call<GetServiceResponse> call = serviceService.getServiceById(Long.parseLong(serviceId));
        call.enqueue(new Callback<GetServiceResponse>() {
            @Override
            public void onResponse(Call<GetServiceResponse> call, Response<GetServiceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    service = response.body();
                    setupViews();
                    Log.i("ServiceEditFragment", "Fetched service: " + service.getId());
                } else {
                    Log.e("ServiceEditFragment", "Error while fetching service: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GetServiceResponse> call, Throwable t) {
                Log.e("ServiceEditFragment", "Network error: " + t.getMessage());
            }
        });
    }

    // Piece of code for seting up views (populating views, setting up listeners, etc. ...)
    private void setupViews() {
        // Populate ChipGroup
        // Chips that match selected event types are set to checked state
        populateEventTypesChipGroup();

        binding.statusTextView.setText(binding.statusTextView.getText() + " " + service.getStatus());

        // RadioGroup change listener
        binding.radioGroupDuration.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selectedRadioButton = group.findViewById(checkedId);
            if (selectedRadioButton == null) return;

            String selectedText = selectedRadioButton.getText().toString();
            Log.d("RadioGroup", "You chose RadioButton: " + selectedText);

            if (selectedText.equals(getString(R.string.fixed))) {
                binding.editTextFixedDuration.setEnabled(true);
                binding.editTextMinDuration.setEnabled(false);
                binding.editTextMaxDuration.setEnabled(false);
                binding.editTextMinDuration.setText("");
                binding.editTextMaxDuration.setText("");
            } else if (selectedText.equals(getString(R.string.min_max))) {
                binding.editTextFixedDuration.setEnabled(false);
                binding.editTextMinDuration.setEnabled(true);
                binding.editTextMaxDuration.setEnabled(true);
                binding.editTextFixedDuration.setText("");
            }
        });

        // Duration fields initialization
        if(service.getFixedDurationInSeconds() != null) {
            binding.radioButtonFixed.setChecked(true);
            binding.radioButtonMinMax.setChecked(false);
            binding.editTextFixedDuration.setText(String.valueOf(convertSecondsToHours(service.getFixedDurationInSeconds())));
        }
        else {
            binding.radioButtonMinMax.setChecked(true);
            binding.radioButtonFixed.setChecked(false);
            binding.editTextMinDuration.setText(String.valueOf(convertSecondsToHours(service.getMinDurationInSeconds())));
            binding.editTextMaxDuration.setText(String.valueOf(convertSecondsToHours(service.getMaxDurationInSeconds())));
        }

        binding.editTextName.setText(service.getName());
        binding.editTextDescription.setText(service.getDescription());
        binding.editTextSpecifics.setText(service.getSpecifics());
        binding.editTextPrice.setText(String.valueOf(service.getPrice()));
        binding.editTextDiscount.setText(String.valueOf(service.getDiscount()));
        binding.checkboxServiceVisible.setChecked(service.getIsVisibleForEventOrganizers());
        binding.checkboxServiceAvailable.setChecked(service.getIsAvailable());
        binding.editTextReservationDeadline.setText(String.valueOf(service.getReservationDeadlineDays()));
        binding.editTextCancellationDeadline.setText(String.valueOf(service.getCancellationDeadlineDays()));
        if (service.getReservationType() == ReservationType.AUTOMATIC)
            binding.radioButtonAuto.setChecked(true);
        else if (service.getReservationType() == ReservationType.MANUAL)
            binding.radioButtonManual.setChecked(true);

        // Edit button listener
        binding.buttonEditService.setOnClickListener(v-> {
            validate();
        });

    }

    // Function for populating chip group with event types
    private void populateEventTypesChipGroup() {
        Call<Collection<GetEventTypeResponse>> call = eventTypeService.getAllEventTypes();
        call.enqueue(new Callback<Collection<GetEventTypeResponse>>() {
            @Override
            public void onResponse(Call<Collection<GetEventTypeResponse>> call, Response<Collection<GetEventTypeResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    binding.chipGroupEventTypes.removeAllViews();

                    for (GetEventTypeResponse eventType : response.body()) {
                        if (eventType.getIsActive()) {
                            Chip chip = new Chip(requireContext());
                            chip.setText(eventType.getName());
                            chip.setCheckable(true);
                            chip.setTag(eventType.getId());
                            chip.setCheckedIconVisible(false);
                            chip.setChipBackgroundColorResource(android.R.color.darker_gray);

                            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                                if (isChecked) {
                                    chip.setChipBackgroundColorResource(android.R.color.holo_purple);
                                } else {
                                    chip.setChipBackgroundColorResource(android.R.color.darker_gray);
                                }
                            });

                            if (service.getEventTypeIds().contains(eventType.getId()))
                                chip.setChecked(true);

                            binding.chipGroupEventTypes.addView(chip);
                        }
                    }
                } else {
                    Log.e("ServiceCreationFragment", "Failed to load event types: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Collection<GetEventTypeResponse>> call, Throwable t) {
                Log.e("ServiceCreationFragment", "Network failure loading event types", t);
            }
        });
    }

    // Input validation and service creation
    private void validate() {
        // Name validation
        String name = binding.editTextName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Name field is required.", Toast.LENGTH_SHORT).show();
            return;
        }
        // Description validation
        String description = binding.editTextDescription.getText().toString().trim();
        if (description.isEmpty()) {
            Toast.makeText(getContext(), "Description field is required.", Toast.LENGTH_SHORT).show();
            return;
        }
        // Specifics validation
        String specifics = binding.editTextSpecifics.getText().toString().trim();
        if (specifics.isEmpty()) {
            Toast.makeText(getContext(), "Specifics field is required.", Toast.LENGTH_SHORT).show();
            return;
        }
        // Price validation
        String priceStr = binding.editTextPrice.getText().toString().trim();
        Double price = null;
        if (priceStr.isEmpty()) {
            Toast.makeText(getContext(), "Price field is required.", Toast.LENGTH_SHORT).show();
            return;
        } else {
            try {
                price = Double.parseDouble(priceStr);
                if (price <= 0) {
                    Toast.makeText(getContext(), "Invalid price input.", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (Exception e) {
                Toast.makeText(getContext(), "Invalid price input.", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        // Discout validation
        String discountStr = binding.editTextDiscount.getText().toString().trim();
        Double discount = null;
        if (discountStr.isEmpty()) {
            Toast.makeText(getContext(), "Discount field is required.", Toast.LENGTH_SHORT).show();
            return;
        } else {
            try {
                discount = Double.parseDouble(discountStr);
                if (discount >= 100) {
                    Toast.makeText(getContext(), "Invalid discount input.", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (Exception e) {
                Toast.makeText(getContext(), "Invalid discount input.", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        // Event types validation
        Collection<Long> selectedEventTypeIds = new ArrayList<>();
        for (int i = 0; i < binding.chipGroupEventTypes.getChildCount(); i++) {
            View view = binding.chipGroupEventTypes.getChildAt(i);
            if (view instanceof Chip) {
                Chip chip = (Chip) view;
                if (chip.isChecked()) {
                    Object tag = chip.getTag();
                    if (tag instanceof Long) {
                        selectedEventTypeIds.add((Long) tag);
                    }
                }
            }
        }
        // IsVisible & IsAvailable
        Boolean isVisible = binding.checkboxServiceVisible.isChecked();
        Boolean isAvailable = binding.checkboxServiceAvailable.isChecked();
        // Duration validation
        Integer fixedDuration = null;
        Integer minDuration = null;
        Integer maxDuration = null;
        if (binding.radioButtonFixed.isChecked()) {
            String fixedDruationStr = binding.editTextFixedDuration.getText().toString().trim();
            try {
                Double fixedDurationHrs = Double.parseDouble(fixedDruationStr);
                fixedDuration = convertHoursToSeconds(fixedDurationHrs);
            }
            catch (Exception e) {
                Toast.makeText(getContext(), "Invalid duration input.", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        else if (binding.radioButtonMinMax.isChecked()) {
            String minDurationStr = binding.editTextMinDuration.getText().toString().trim();
            String maxDurationStr = binding.editTextMaxDuration.getText().toString().trim();
            try {
                Double minDurationHrs = Double.parseDouble(minDurationStr);
                Double maxDurationHrs = Double.parseDouble(maxDurationStr);
                minDuration = convertHoursToSeconds(minDurationHrs);
                maxDuration = convertHoursToSeconds(maxDurationHrs);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Invalid duration input.", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            Toast.makeText(getContext(), "Duration input is required.", Toast.LENGTH_SHORT).show();
            return;
        }
        // Reservation Cancellation deadline validation
        Integer reservationDeadlineDays = null;
        Integer cancellationDeadlineDays = null;
        try {
            reservationDeadlineDays = Integer.parseInt(binding.editTextReservationDeadline.getText().toString().trim());
            cancellationDeadlineDays = Integer.parseInt(binding.editTextCancellationDeadline.getText().toString().trim());
        } catch (Exception e) {
            Toast.makeText(getContext(), "Reservation and canellation deadlines inputs are required.", Toast.LENGTH_SHORT).show();
            return;
        }
        // ReservationType validation
        ReservationType reservationType;
        if (binding.radioButtonAuto.isChecked()) {
            reservationType = ReservationType.AUTOMATIC;
        } else if (binding.radioButtonManual.isChecked()) {
            reservationType = ReservationType.MANUAL;
        } else {
            Toast.makeText(getContext(), "Reservation type field is required.", Toast.LENGTH_SHORT).show();
            return;
        }

        UpdateServiceRequest request = UpdateServiceRequest.builder()
                .name(name)
                .description(description)
                .price(price)
                .discount(discount)
                .specifics(specifics)
                .isDeleted(false)
                .isVisibleForEventOrganizers(isVisible)
                .isAvailable(isAvailable)
                .fixedDurationInSeconds(fixedDuration)
                .minDurationInSeconds(minDuration)
                .maxDurationInSeconds(maxDuration)
                .reservationDeadlineDays(reservationDeadlineDays)
                .cancellationDeadlineDays(cancellationDeadlineDays)
                .reservationType(reservationType)
                .categoryId(service.getCategoryId())
                .eventTypeIds(selectedEventTypeIds)
                .build();

        editService(request);
    }

    private void editService(UpdateServiceRequest request) {
        serviceService.updateService(Long.parseLong(serviceId), request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Service updated successfuly", Toast.LENGTH_SHORT).show();
                    Log.d("ServiceEditFragment", "Service updated successfully");
                } else {
                    Log.e("ServiceEditFragmente", "Update failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("ServiceEditFragment", "Network error", t);
            }
        });
    }

    public int convertHoursToSeconds(double hours) {
        int hoursPart = (int) Math.floor(hours);
        double minutesPart = (hours - hoursPart) * 60;

        int totalSeconds = (hoursPart * 60 * 60) + (int) Math.round(minutesPart) * 60;
        return totalSeconds;
    }

    public double convertSecondsToHours(int seconds) {
        return seconds / 3600.0;
    }
}