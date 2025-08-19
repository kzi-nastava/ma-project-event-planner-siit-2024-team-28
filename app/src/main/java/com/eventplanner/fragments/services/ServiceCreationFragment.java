package com.eventplanner.fragments.services;

import static android.app.Activity.RESULT_OK;

import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.eventplanner.R;
import com.eventplanner.databinding.FragmentServiceCreationBinding;
import com.eventplanner.model.enums.DurationType;
import com.eventplanner.model.enums.RequestStatus;
import com.eventplanner.model.enums.ReservationType;
import com.eventplanner.model.enums.SolutionStatus;
import com.eventplanner.model.requests.services.CreateServiceRequest;
import com.eventplanner.model.requests.solutionCategories.CreatePendingCategoryRequest;
import com.eventplanner.model.requests.solutionCategories.CreateSolutionCategoryRequest;
import com.eventplanner.model.responses.eventTypes.GetEventTypeResponse;
import com.eventplanner.model.responses.solutionCateogries.GetSolutionCategoryResponse;
import com.eventplanner.services.EventTypeService;
import com.eventplanner.services.ServiceService;
import com.eventplanner.services.SolutionCategoryService;
import com.eventplanner.utils.AuthUtils;
import com.eventplanner.utils.Base64Util;
import com.eventplanner.utils.HttpUtils;
import com.eventplanner.utils.RequestCodes;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServiceCreationFragment extends Fragment {

    private FragmentServiceCreationBinding binding;
    private List<GetSolutionCategoryResponse> allCategories;
    private ServiceService serviceService;
    private SolutionCategoryService categoryService;
    private EventTypeService eventTypeService;
    private List<String> base64Images;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        serviceService = HttpUtils.getServiceService();
        categoryService = HttpUtils.getSolutionCategoryService();
        eventTypeService = HttpUtils.getEventTypeService();
        base64Images = new ArrayList<>();
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
                binding.editTextMinDuration.setText("");
                binding.editTextMaxDuration.setText("");
            } else if (selectedText.equals(getString(R.string.min_max))) {
                binding.editTextFixedDuration.setEnabled(false);
                binding.editTextMinDuration.setEnabled(true);
                binding.editTextMaxDuration.setEnabled(true);
                binding.editTextFixedDuration.setText("");
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

        // Button for triggering image selection
        binding.buttonSelectImages.setOnClickListener(v -> {
            openImageSelection();
        });

        // Setting up number of selected images indicator
        String numberOfSelectedImagesString = getString(R.string.number_of_selected_images, base64Images.size());
        binding.numberOfSelectedImages.setText(numberOfSelectedImagesString);

        // Create button listener
        binding.buttonCreateService.setOnClickListener(v-> {
            validate();
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
                    Toast.makeText(getContext(), "Price cannot be negative or 0.", Toast.LENGTH_SHORT).show();
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
                if (discount >= 100 || discount < 0) {
                    Toast.makeText(getContext(), "Discount has to be between 0 and 99.", Toast.LENGTH_SHORT).show();
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
        DurationType durationType;
        if (binding.radioButtonFixed.isChecked()) {
            durationType = DurationType.FIXED;
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
            durationType = DurationType.MINMAX;
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
        // Category validation
        // Two different creation calls -> 1. regular creation call for selected existing category
        //                              -> 2. creation call for custom category
        boolean customCategoryCreation = false;
        String customCategory = null;
        Long selectedCategoryId = null;
        int selectedCategoryPosition = binding.spinnerCategory.getSelectedItemPosition();
        if (selectedCategoryPosition > 0) {
            GetSolutionCategoryResponse selectedCategory = allCategories.get(selectedCategoryPosition - 1);
            selectedCategoryId = selectedCategory.getId();
            customCategoryCreation = false;
        }
        else if (!binding.editTextCustomCategory.getText().toString().trim().isEmpty()) {
            customCategory = binding.editTextCustomCategory.getText().toString().trim();
            customCategoryCreation = true;
        }
        else {
            Toast.makeText(getContext(), "Category field is required input.", Toast.LENGTH_SHORT).show();
            return;
        }

        CreateServiceRequest request = CreateServiceRequest.builder()
                .name(name)
                .description(description)
                .price(price)
                .discount(discount)
                .imageBase64(this.base64Images)
                .specifics(specifics)
                .isVisibleForEventOrganizers(isVisible)
                .isAvailable(isAvailable)
                .durationType(durationType)
                .fixedDurationInSeconds(fixedDuration)
                .minDurationInSeconds(minDuration)
                .maxDurationInSeconds(maxDuration)
                .reservationDeadlineDays(reservationDeadlineDays)
                .cancellationDeadlineDays(cancellationDeadlineDays)
                .reservationType(reservationType)
                .categoryId(selectedCategoryId)
                .businessOwnerId(AuthUtils.getUserId(requireContext()))
                .eventTypeIds(selectedEventTypeIds)
                .status((customCategoryCreation) ? SolutionStatus.PENDING : SolutionStatus.ACTIVE ) // If category is custom we are creating PENDING service
                .build();

        if(!customCategoryCreation) {
            createService(request);
        } else {
            createCustomCategory(request, customCategory);
        }
    }

    // Function for making request to backend for creating new Service
    private void createService(CreateServiceRequest request) {
        serviceService.createService(request).enqueue(new Callback<Long>() {
            @Override
            public void onResponse(Call<Long> call, Response<Long> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Long newServiceId = response.body();
                    Toast.makeText(getContext(), "Service created! ID: " + newServiceId, Toast.LENGTH_SHORT).show();
                } else {
                    String message = "Unknown error.";
                    if (response.errorBody() != null) {
                        try {
                            String errorString = response.errorBody().string();
                            Log.e("ServiceCreationFragment", "Error body: " + errorString);
                            message = new JSONObject(errorString).optString("error", message);
                        } catch (Exception ignored) {}
                    }
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    Log.e("ServiceCreationFragment", "Error: " + message);
                }
            }

            @Override
            public void onFailure(Call<Long> call, Throwable t) {
                Toast.makeText(getContext(), "An error has occured while creating service.", Toast.LENGTH_SHORT).show();
                Log.i("ServiceCreationFragment", "Network failure: " + t.getMessage());
            }
        });
    }

    // Function for making request to backend for creating new Category and then for creating new Service
    private void createCustomCategory(CreateServiceRequest serviceRequest, String customCategoryName) {
        CreatePendingCategoryRequest request = new CreatePendingCategoryRequest(customCategoryName);
        categoryService.createPendingCategory(request).enqueue(new Callback<Long>() {
            @Override
            public void onResponse(Call<Long> call, Response<Long> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Long newCategoryId = response.body();
                    Log.i("ServiceCreationFragment", "Category created! ID: " + newCategoryId);
                    serviceRequest.setCategoryId(newCategoryId);
                    createService(serviceRequest);
                } else {
                    String message = "Unknown error.";
                    if (response.errorBody() != null) {
                        try {
                            String errorString = response.errorBody().string();
                            Log.e("ServiceCreationFragment", "Error body: " + errorString);
                            message = new JSONObject(errorString).optString("error", message);
                        } catch (Exception ignored) {}
                    }
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    Log.e("ServiceCreationFragment", "Error: " + message);
                }
            }

            @Override
            public void onFailure(Call<Long> call, Throwable t) {
                Toast.makeText(getContext(), "An error has occured.", Toast.LENGTH_SHORT).show();
                Log.i("ServiceCreationFragment", "Network failure: " + t.getMessage());
            }
        });
    }

    // populating Category spinner with possible categories
    // IMPORTANT: when using selected value use INDEX + 1 since first element is null placeholder
    private void populateCategoriesFilter() {
        Spinner spinner = binding.spinnerCategory;

        Call<Collection<GetSolutionCategoryResponse>> call = categoryService.getAcceptedCategories();
        call.enqueue(new Callback<Collection<GetSolutionCategoryResponse>>() {
            @Override
            public void onResponse(Call<Collection<GetSolutionCategoryResponse>> call, Response<Collection<GetSolutionCategoryResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allCategories = new ArrayList<>();

                    List<String> categoryNames = new ArrayList<>();
                    categoryNames.add("Select a category...");

                    for (GetSolutionCategoryResponse category : response.body()) {
                        allCategories.add(category);
                        categoryNames.add(category.getName());
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

    public int convertHoursToSeconds(double hours) {
        int hoursPart = (int) Math.floor(hours);
        double minutesPart = (hours - hoursPart) * 60;

        int totalSeconds = (hoursPart * 60 * 60) + (int) Math.round(minutesPart) * 60;
        return totalSeconds;
    }

    // Function for resolving image selection
    public void openImageSelection() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select Pictures"), RequestCodes.REQUEST_CODE_PICK_IMAGES); //REQUEST_CODE_PICK_IMAGES -> code for intents used in image selection
    }

    // Processing Intent results
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RequestCodes.REQUEST_CODE_PICK_IMAGES && resultCode == RESULT_OK && data != null) {
            List<Uri> imageUris = new ArrayList<>();

            if (data.getClipData() != null) { // Multiple pictures selected
                ClipData clipData = data.getClipData();
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    Uri uri = clipData.getItemAt(i).getUri();
                    imageUris.add(uri);
                }
            } else if (data.getData() != null) { // One picture selected
                imageUris.add(data.getData());
            }

            proccessImages(imageUris);
        }
    }

    private void proccessImages(List<Uri> imageUris) {
        if(imageUris == null) {
            return;
        }

        // List of Uri elements encodes to base64
        List<String> base64Images = new ArrayList<>();
        for (Uri uri : imageUris) {
            Bitmap bitmap = Base64Util.getBitmapFromUri(requireContext(), uri);
            if (bitmap != null) {
                String base64 = Base64Util.encodeImageToBase64(bitmap);
                base64Images.add(base64);
            }
        }

        // Saving images in private field
        this.base64Images = base64Images;

        // Setting up number of selected images indicator
        String numberOfSelectedImagesString = getString(R.string.number_of_selected_images, this.base64Images.size());
        binding.numberOfSelectedImages.setText(numberOfSelectedImagesString);
    }
}