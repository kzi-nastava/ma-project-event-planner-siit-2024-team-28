package com.eventplanner.fragments.services;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.eventplanner.R;
import com.eventplanner.databinding.FragmentServiceCreationBinding;
import com.eventplanner.model.enums.RequestStatus;
import com.eventplanner.model.responses.eventTypes.GetEventTypeResponse;
import com.eventplanner.model.responses.solutionCateogries.GetSolutionCategoryResponse;
import com.eventplanner.services.EventTypeService;
import com.eventplanner.services.ServiceService;
import com.eventplanner.services.SolutionCategoryService;
import com.eventplanner.utils.HttpUtils;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServiceCreationFragment extends Fragment {

    private FragmentServiceCreationBinding binding;
    private ServiceService serviceService;
    private SolutionCategoryService categoryService;
    private EventTypeService eventTypeService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        serviceService = HttpUtils.getServiceService();
        categoryService = HttpUtils.getSolutionCategoryService();
        eventTypeService = HttpUtils.getEventTypeService();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentServiceCreationBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initial view setup
        setupViews();


        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // prevent memory leaks
    }

    // Piece of code for seting up views (populating views, setting up listeners, etc. ...)
    private void setupViews() {
        // Populate categories spinner
        populateCategoriesFilter();

        // Populate ChipGroup
        populateEventTypesChipGroup();

        // Disable all duration EditTexts initially
        binding.editTextFixedDuration.setEnabled(false);
        binding.editTextMinDuration.setEnabled(false);
        binding.editTextMaxDuration.setEnabled(false);

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
            } else if (selectedText.equals(getString(R.string.min_max))) {
                binding.editTextFixedDuration.setEnabled(false);
                binding.editTextMinDuration.setEnabled(true);
                binding.editTextMaxDuration.setEnabled(true);
            }
        });

        // EditText custom category on change listener
        binding.editTextCustomCategory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString().trim();
                if (input.isEmpty()) {
                    // If there is no text set category spinner to enabled
                    binding.spinnerCategory.setEnabled(true);
                } else {
                    // If there is a text set category spinner to disabled
                    binding.spinnerCategory.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        // Create button listener
        binding.buttonCreateService.setOnClickListener(v-> {
            createService();
        });

    }

    // Input validation and service creation
    private void createService() {

    }

    // populating Category spinner with possible categories
    // IMPORTANT: when using selected value use INDEX + 1 since first element iz null placeholder
    private void populateCategoriesFilter() {
        Spinner spinner = binding.spinnerCategory;

        Call<Collection<GetSolutionCategoryResponse>> call = categoryService.getAllSolutionCategories();
        call.enqueue(new Callback<Collection<GetSolutionCategoryResponse>>() {
            @Override
            public void onResponse(Call<Collection<GetSolutionCategoryResponse>> call, Response<Collection<GetSolutionCategoryResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<GetSolutionCategoryResponse> allCategories = new ArrayList<>();

                    List<String> categoryNames = new ArrayList<>();
                    categoryNames.add("Select a category...");

                    for (GetSolutionCategoryResponse category : response.body()) {
                        if (category.getRequestStatus().equals(RequestStatus.ACCEPTED) && !category.getIsDeleted()) {
                            allCategories.add(category);
                            categoryNames.add(category.getName());
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            getContext(),
                            android.R.layout.simple_spinner_item,
                            categoryNames
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(adapter);

                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (position == 0) {
                                binding.editTextCustomCategory.setEnabled(true);
                            } else {
                                binding.editTextCustomCategory.setEnabled(false);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });
                } else {
                    Log.e("ServiceCreationFragment", "Failed to fetch categories: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Collection<GetSolutionCategoryResponse>> call, Throwable t) {
                Log.e("ServiceCreationFragment", "Network failure", t);
            }
        });
    }

    // Function for populating chip group with event types
    private void populateEventTypesChipGroup() {
        Call<Collection<GetEventTypeResponse>> call = eventTypeService.getAllEventTypes();
        call.enqueue(new Callback<Collection<GetEventTypeResponse>>() {
            @Override
            public void onResponse(Call<Collection<GetEventTypeResponse>> call, Response<Collection<GetEventTypeResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    binding.chipGroupEventTypes.removeAllViews(); // očisti postojeće

                    for (GetEventTypeResponse eventType : response.body()) {
                        if (eventType.getIsActive()) {  // filtriranje ako treba
                            Chip chip = new Chip(requireContext());
                            chip.setText(eventType.getName());
                            chip.setCheckable(true);
                            chip.setCheckedIconVisible(false);
                            chip.setChipBackgroundColorResource(android.R.color.darker_gray);

                            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                                if (isChecked) {
                                    chip.setChipBackgroundColorResource(android.R.color.holo_purple);
                                } else {
                                    chip.setChipBackgroundColorResource(android.R.color.darker_gray);
                                }
                            });

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
}