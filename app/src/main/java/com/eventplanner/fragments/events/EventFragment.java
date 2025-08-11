package com.eventplanner.fragments.events;

import static com.eventplanner.utils.Base64Util.decodeBase64ToBitmap;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.eventplanner.R;
import com.eventplanner.databinding.FragmentEventBinding;
import com.eventplanner.fragments.activities.ActivitiesDialogFragment;
import com.eventplanner.fragments.locations.LocationDialogFragment;
import com.eventplanner.model.enums.PrivacyType;
import com.eventplanner.model.requests.activities.CreateActivityRequest;
import com.eventplanner.model.requests.eventReviews.CreateEventReviewRequest;
import com.eventplanner.model.requests.eventReviews.UpdateEventReviewRequest;
import com.eventplanner.model.requests.events.CreateEventRequest;
import com.eventplanner.model.requests.events.UpdateEventRequest;
import com.eventplanner.model.requests.locations.CreateLocationRequest;
import com.eventplanner.model.responses.activities.GetActivityResponse;
import com.eventplanner.model.responses.eventReviews.GetEventReviewResponse;
import com.eventplanner.model.responses.eventTypes.GetEventTypeResponse;
import com.eventplanner.model.responses.events.GetEventResponse;
import com.eventplanner.services.EventReviewService;
import com.eventplanner.services.EventService;
import com.eventplanner.services.EventTypeService;
import com.eventplanner.utils.AuthUtils;
import com.eventplanner.utils.HttpUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventFragment extends Fragment {
    private FragmentEventBinding binding;
    private EventService eventService;
    private EventTypeService eventTypeService;
    private Long eventId;
    private Long eventOrganizerId;
    private boolean isEditMode = false;
    private boolean isFavorite = false;
    private CreateLocationRequest location;
    private List<CreateActivityRequest> activities = new ArrayList<>();
    private List<GetEventTypeResponse> eventTypes = new ArrayList<>();
    private String imageBase64;
    private EventReviewService eventReviewService;
    private GetEventReviewResponse currentUserReview;
    private RatingBar ratingBar;
    private Button submitReviewButton, deleteReviewButton;
    private LinearLayout reviewSection;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        HttpUtils.initialize(getContext());

        binding = FragmentEventBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize services
        eventService = HttpUtils.getEventService();
        eventTypeService = HttpUtils.getEventTypeService();

        // Get event ID from arguments if editing
        if (getArguments() != null) {
            eventId = getArguments().getLong("eventId");
            isEditMode = true;
            loadEventDetails();
        } else {
            setupForm();
            setupButtons();
        }

        loadEventTypes();
        setupPrivacyTypeSpinner();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 123);
        }

        binding.deleteButton.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
        binding.downloadGuestListButton.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
        binding.downloadDetailsButton.setVisibility(isEditMode ? View.VISIBLE : View.GONE);

        eventReviewService = HttpUtils.getEventReviewService();

        reviewSection = binding.getRoot().findViewById(R.id.reviewSection);
        ratingBar = binding.getRoot().findViewById(R.id.ratingBar);
        submitReviewButton = binding.getRoot().findViewById(R.id.submitReviewButton);
        deleteReviewButton = binding.getRoot().findViewById(R.id.deleteReviewButton);

        if (AuthUtils.getUserId(getContext()) != null) {
            submitReviewButton.setOnClickListener(v -> submitReview());
            deleteReviewButton.setOnClickListener(v -> deleteReview());

            if (isEditMode) {
                reviewSection.setVisibility(View.VISIBLE);
                loadUserReview(AuthUtils.getUserId(getContext()), eventId);
            }
        } else {
            submitReviewButton.setVisibility(View.GONE);
            deleteReviewButton.setVisibility(View.GONE);
        }
    }

    private void setupForm() {
        // Disable form if user is not organizer (for edit mode)
        if (isEditMode && !isOrganizer()) {
            disableForm();
        }
    }

    private boolean isOrganizer() {
        Long currentUserId = AuthUtils.getUserId(getContext());
        return currentUserId != null && currentUserId.equals(eventOrganizerId);
    }

    private void disableForm() {
        // Disable all input fields
        binding.name.setEnabled(false);
        binding.name.setFocusable(false);
        binding.name.setClickable(false);

        binding.description.setEnabled(false);
        binding.description.setFocusable(false);
        binding.description.setClickable(false);

        binding.maxParticipants.setEnabled(false);
        binding.maxParticipants.setFocusable(false);
        binding.maxParticipants.setClickable(false);

        binding.privacyType.setEnabled(false);
        binding.privacyType.setFocusable(false);
        binding.privacyType.setClickable(false);

        binding.eventType.setEnabled(false);
        binding.eventType.setFocusable(false);
        binding.eventType.setClickable(false);

        binding.startDate.setEnabled(false);
        binding.startDate.setFocusable(false);
        binding.startDate.setClickable(false);

        binding.endDate.setEnabled(false);
        binding.endDate.setFocusable(false);
        binding.endDate.setClickable(false);

        // Disable buttons
        binding.imageUploadButton.setEnabled(false);
        binding.locationButton.setEnabled(false);
        binding.activitiesButton.setEnabled(false);
        binding.submitButton.setEnabled(false);
        binding.deleteButton.setEnabled(false);

        // Also disable the TextInputLayouts to prevent any interaction
        binding.nameLayout.setEnabled(false);
        binding.descriptionLayout.setEnabled(false);
        binding.maxParticipantsLayout.setEnabled(false);
        binding.startDateLayout.setEnabled(false);
        binding.endDateLayout.setEnabled(false);
    }

    private void loadEventDetails() {
        eventService.getEventById(eventId).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<GetEventResponse> call, @NonNull Response<GetEventResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GetEventResponse event = response.body();
                    eventOrganizerId = event.getEventOrganizerId();

                    // Check if current user is organizer
                    if (!isOrganizer()) {
                        disableForm();
                    }

                    populateForm(event);
                    setupButtons();
                } else {
                    Toast.makeText(requireContext(), "Failed to load event", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<GetEventResponse> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateForm(GetEventResponse event) {
        // Set basic fields
        binding.name.setText(event.getName());
        binding.description.setText(event.getDescription());
        binding.maxParticipants.setText(String.valueOf(event.getMaxParticipants()));
        binding.startDate.setText(event.getStartDate());
        binding.endDate.setText(event.getEndDate());

        // Set privacy type safely
        if (event.getPrivacyType() != null) {
            binding.getRoot().post(() -> {
                ArrayAdapter<PrivacyType> privacyAdapter = (ArrayAdapter<PrivacyType>) binding.privacyType.getAdapter();
                for (int i = 0; i < privacyAdapter.getCount(); i++) {
                    if (privacyAdapter.getItem(i).name().equals(event.getPrivacyType())) {
                        binding.privacyType.setText(privacyAdapter.getItem(i).toString(), false);
                        break;
                    }
                }
            });
        }

        // Set event type safely
        if (event.getEventTypeId() != null && !eventTypes.isEmpty()) {
            binding.getRoot().post(() -> {
                for (int i = 0; i < eventTypes.size(); i++) {
                    if (eventTypes.get(i).getId().equals(event.getEventTypeId())) {
                        binding.eventType.setText(eventTypes.get(i).getName(), false);
                        break;
                    }
                }
            });
        } else {
            binding.getRoot().post(() -> {
                binding.eventType.setText("All", false);
            });
        }

        // Set location
        if (event.getLocation() != null) {
            location = new CreateLocationRequest(
                    event.getLocation().getName(),
                    event.getLocation().getAddress(),
                    event.getLocation().getLatitude(),
                    event.getLocation().getLongitude()
            );
            updateLocationSummary();
        }

        // Set activities
        if (event.getActivities() != null) {
            activities.clear();
            for (GetActivityResponse activity : event.getActivities()) {
                activities.add(new CreateActivityRequest(
                        activity.getName(),
                        activity.getDescription(),
                        activity.getStartTime(),
                        activity.getEndTime()
                ));
            }
            updateActivitiesSummary();
        }

        // Set image
        if (event.getImageBase64() != null && !event.getImageBase64().isEmpty()) {
            imageBase64 = event.getImageBase64();
            binding.eventImage.setImageBitmap(decodeBase64ToBitmap(imageBase64));
        }

        // Set favorite state
        isFavorite = event.isFavoriteForCurrentUser();
        updateFavoriteButton();
    }

    private void loadEventTypes() {
        eventTypeService.getAllEventTypes().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Collection<GetEventTypeResponse>> call, @NonNull Response<Collection<GetEventTypeResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    eventTypes = response.body().stream().collect(Collectors.toList());
                    setupEventTypeSpinner();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Collection<GetEventTypeResponse>> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Failed to load event types", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupEventTypeSpinner() {
        List<String> typeNames = new ArrayList<>();
        typeNames.add("All"); // First option is always "All"
        for (GetEventTypeResponse type : eventTypes) {
            typeNames.add(type.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                typeNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.eventType.setAdapter(adapter);

        // Set default selection to "All" if not in edit mode
        if (!isEditMode) {
            binding.eventType.setText("All", false);
        }
    }

    private void setupButtons() {
        binding.imageUploadButton.setOnClickListener(v -> openImagePicker());
        binding.locationButton.setOnClickListener(v -> openLocationDialog());
        binding.activitiesButton.setOnClickListener(v -> openActivitiesDialog());
        binding.submitButton.setOnClickListener(v -> submitForm());
        binding.cancelButton.setOnClickListener(v -> navigateBack());
        binding.deleteButton.setOnClickListener(v -> confirmDelete());
        binding.downloadGuestListButton.setOnClickListener(v -> downloadGuestList());
        binding.downloadDetailsButton.setOnClickListener(v -> downloadEventDetails());
        binding.favoriteButton.setOnClickListener(v -> toggleFavorite());
        binding.startDate.setOnClickListener(v -> showDatePicker(binding.startDate));
        binding.endDate.setOnClickListener(v -> showDatePicker(binding.endDate));
        binding.startDate.setFocusable(false);
        binding.startDate.setClickable(true);
        binding.endDate.setFocusable(false);
        binding.endDate.setClickable(true);

        // Only show action buttons if in edit mode and user is organizer
        boolean showActionButtons = isEditMode && isOrganizer();
        binding.submitButton.setVisibility(showActionButtons ? View.VISIBLE : View.GONE);
        binding.deleteButton.setVisibility(showActionButtons ? View.VISIBLE : View.GONE);

        // Show download buttons if in edit mode (regardless of organizer status)
        binding.downloadGuestListButton.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
        binding.downloadDetailsButton.setVisibility(isEditMode ? View.VISIBLE : View.GONE);

        // Favorite button is always visible but disabled if not logged in
        binding.favoriteButton.setEnabled(AuthUtils.getUserId(getContext()) != null);

        // Disable image upload if not organizer
        if (isEditMode && !isOrganizer()) {
            binding.imageUploadButton.setEnabled(false);
        }
    }

    private void openImagePicker() {
        // Implement image picker intent
    }

    private void openLocationDialog() {
        boolean isReadOnly = !isOrganizer();
        LocationDialogFragment dialog = LocationDialogFragment.newInstance(location, isReadOnly);
        dialog.show(getChildFragmentManager(), "LocationDialogFragment");

        getChildFragmentManager().setFragmentResultListener("location_request", this, (requestKey, result) -> {
            String locationJson = result.getString("location");
            Gson gson = new Gson();
            location = gson.fromJson(locationJson, CreateLocationRequest.class);
            updateLocationSummary();
        });
    }

    private void openActivitiesDialog() {
        boolean isReadOnly = !isOrganizer();
        ActivitiesDialogFragment dialog = ActivitiesDialogFragment.newInstance(activities, isReadOnly);
        dialog.show(getChildFragmentManager(), "ActivitiesDialogFragment");

        getChildFragmentManager().setFragmentResultListener("activities_request", this, (requestKey, result) -> {
            String activitiesJson = result.getString("activities");
            Gson gson = new Gson();
            Type type = new TypeToken<List<CreateActivityRequest>>() {
            }.getType();
            activities = gson.fromJson(activitiesJson, type);
            updateActivitiesSummary();
        });
    }

    private PrivacyType getSelectedPrivacyType() {
        // Get the currently displayed text
        String selectedText = binding.privacyType.getText().toString();

        // Find which enum value matches this text
        for (PrivacyType type : PrivacyType.values()) {
            if (type.toString().equals(selectedText)) {
                return type;
            }
        }

        // Default to PUBLIC if nothing matches (or handle differently if needed)
        return PrivacyType.PUBLIC;
    }

    private void submitForm() {
        if (!validateForm()) {
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        LocalDate startDate, endDate;

        try {
            startDate = LocalDate.parse(binding.startDate.getText().toString(), formatter);
            endDate = LocalDate.parse(binding.endDate.getText().toString(), formatter);
        } catch (DateTimeParseException e) {
            Toast.makeText(requireContext(), "Invalid date format. Use YYYY-MM-DD", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isEditMode) {
            UpdateEventRequest request = new UpdateEventRequest(
                    binding.name.getText().toString(),
                    binding.description.getText().toString(),
                    Integer.parseInt(binding.maxParticipants.getText().toString()),
                    getSelectedPrivacyType(),
                    getSelectedEventTypeId(),
                    location,
                    startDate,
                    endDate,
                    new ArrayList<>(activities),
                    imageBase64
            );

            updateEvent(request);
        } else {
            CreateEventRequest request = new CreateEventRequest(
                    binding.name.getText().toString(),
                    binding.description.getText().toString(),
                    Integer.parseInt(binding.maxParticipants.getText().toString()),
                    getSelectedPrivacyType(),
                    getSelectedEventTypeId(),
                    location,
                    startDate,
                    endDate,
                    new ArrayList<>(activities),
                    imageBase64
            );

            createEvent(request);
        }
    }

    private boolean validateForm() {
        if (binding.name.getText().toString().isEmpty()) {
            Toast.makeText(requireContext(), "Name is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (binding.description.getText().toString().isEmpty()) {
            Toast.makeText(requireContext(), "Description is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (binding.maxParticipants.getText().toString().isEmpty()) {
            Toast.makeText(requireContext(), "Max participants is required", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            LocalDate startDate = LocalDate.parse(binding.startDate.getText().toString());
            LocalDate endDate = LocalDate.parse(binding.endDate.getText().toString());

            if (endDate.isBefore(startDate)) {
                Toast.makeText(requireContext(), "End date must be after start date", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (DateTimeParseException e) {
            Toast.makeText(requireContext(), "Invalid date format", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private Long getSelectedEventTypeId() {
        String selectedName = binding.eventType.getText().toString();
        if (selectedName.equals("All")) {
            return null;
        }
        for (GetEventTypeResponse type : eventTypes) {
            if (type.getName().equals(selectedName)) {
                return type.getId();
            }
        }
        return null;
    }

    private void createEvent(CreateEventRequest request) {
        eventService.createEvent(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Event created successfully", Toast.LENGTH_SHORT).show();
                    navigateBack();
                } else {
                    Toast.makeText(requireContext(), "Failed to create event", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEvent(UpdateEventRequest request) {
        eventService.updateEvent(eventId, request).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Event updated successfully", Toast.LENGTH_SHORT).show();
                    navigateBack();
                } else {
                    Toast.makeText(requireContext(), "Failed to update event", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDelete() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete this event?")
                .setPositiveButton("Delete", (dialog, which) -> deleteEvent())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteEvent() {
        eventService.deleteEventById(eventId).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Event deleted successfully", Toast.LENGTH_SHORT).show();
                    navigateBack();
                } else {
                    Toast.makeText(requireContext(), "Failed to delete event", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void downloadGuestList() {
        eventService.getGuestListPdf(eventId).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Save PDF file
                    savePdfFile(response.body(), "event_" + eventId + "_guests.pdf");
                } else {
                    Toast.makeText(requireContext(), "Failed to download guest list", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void downloadEventDetails() {
        eventService.getEventDetails(eventId).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Save PDF file
                    savePdfFile(response.body(), "event_" + eventId + "_details.pdf");
                } else {
                    Toast.makeText(requireContext(), "Failed to download event details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void savePdfFile(ResponseBody body, String fileName) {
        OutputStream outputStream = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Use MediaStore for Android 10 and above
                ContentResolver resolver = requireContext().getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                contentValues.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
                contentValues.put(MediaStore.Downloads.IS_PENDING, 1);

                Uri collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                Uri fileUri = resolver.insert(collection, contentValues);
                if (fileUri == null) {
                    Toast.makeText(requireContext(), "Failed to create file", Toast.LENGTH_LONG).show();
                    return;
                }

                outputStream = resolver.openOutputStream(fileUri);
                if (outputStream == null) {
                    Toast.makeText(requireContext(), "Failed to open file", Toast.LENGTH_LONG).show();
                    return;
                }

                writeResponseBodyToStream(body, outputStream);

                contentValues.clear();
                contentValues.put(MediaStore.Downloads.IS_PENDING, 0);
                resolver.update(fileUri, contentValues, null, null);

            } else {
                // For Android 9 and below
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!downloadsDir.exists()) downloadsDir.mkdirs();

                File file = new File(downloadsDir, fileName);
                outputStream = new FileOutputStream(file);
                writeResponseBodyToStream(body, outputStream);

                // Notify media scanner
                MediaScannerConnection.scanFile(requireContext(), new String[]{file.getAbsolutePath()}, null, null);
            }

            Toast.makeText(requireContext(), "PDF saved to Downloads", Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            Toast.makeText(requireContext(), "Error saving file: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            try {
                if (outputStream != null) outputStream.close();
            } catch (IOException ignored) {
            }
        }
    }

    private void writeResponseBodyToStream(ResponseBody body, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[4096];
        int bytesRead;
        InputStream inputStream = body.byteStream();

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        outputStream.flush();
        inputStream.close();
    }

    private void toggleFavorite() {
        eventService.toggleEventFavoriteStateForCurrentUser(eventId).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    isFavorite = !isFavorite;
                    updateFavoriteButton();
                    String message = isFavorite ? "Added to favorites" : "Removed from favorites";
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Failed to update favorite status", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateFavoriteButton() {
        binding.favoriteButton.setText(isFavorite ? "Unfavorite" : "Favorite");
    }

    private void updateLocationSummary() {
        if (location != null) {
            binding.locationSummary.setVisibility(View.VISIBLE);
            binding.locationName.setText(location.getName());
            binding.locationAddress.setText(location.getAddress());
        } else {
            binding.locationSummary.setVisibility(View.GONE);
        }
    }

    private void updateActivitiesSummary() {
        if (!activities.isEmpty()) {
            binding.activitiesList.removeAllViews();
            for (CreateActivityRequest activity : activities) {
                View activityView = LayoutInflater.from(requireContext())
                        .inflate(R.layout.item_activity_preview, binding.activitiesList, false);

                TextView nameView = activityView.findViewById(R.id.activity_name);
                TextView descView = activityView.findViewById(R.id.activity_description);

                nameView.setText(activity.getName());
                descView.setText(activity.getDescription());

                binding.activitiesList.addView(activityView);
            }
            binding.activitiesList.setVisibility(View.VISIBLE);
        } else {
            binding.activitiesList.setVisibility(View.GONE);
        }
    }

    private void navigateBack() {
        try {
            NavController navController = Navigation.findNavController(requireView());
            if (!navController.popBackStack()) {
                navController.navigate(R.id.nav_home);
            }
        } catch (IllegalStateException e) {
            requireActivity().onBackPressed();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupPrivacyTypeSpinner() {
        PrivacyType[] privacyValues = PrivacyType.values();
        ArrayAdapter<PrivacyType> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                privacyValues
        );
        binding.privacyType.setAdapter(adapter);
    }

    // Add this to your EventFragment class
    private void showDatePicker(TextInputEditText editText) {
        LocalDate currentDate = LocalDate.now();
        if (!editText.getText().toString().isEmpty()) {
            try {
                currentDate = LocalDate.parse(editText.getText().toString());
            } catch (DateTimeParseException ignored) {
            }
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    LocalDate selectedDate = LocalDate.of(year, month + 1, dayOfMonth);
                    editText.setText(selectedDate.toString());
                },
                currentDate.getYear(),
                currentDate.getMonthValue() - 1,
                currentDate.getDayOfMonth()
        );
        datePickerDialog.show();
    }

    private void loadUserReview(Long userId, Long eventId) {
        eventReviewService.getReviewByUserAndEvent(userId, eventId).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<GetEventReviewResponse> call, @NonNull Response<GetEventReviewResponse> response) {
                if (response.isSuccessful()) {
                    currentUserReview = response.body();
                    if (currentUserReview != null) {
                        ratingBar.setRating(currentUserReview.getRating());
                        deleteReviewButton.setVisibility(View.VISIBLE);
                        submitReviewButton.setText(getString(R.string.submit_review));
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<GetEventReviewResponse> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), R.string.failed_to_load_review, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitReview() {
        short rating = (short) ratingBar.getRating();
        if (rating < 1 || rating > 5) {
            Toast.makeText(requireContext(), getString(R.string.please_select_a_rating_1_5), Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUserReview != null) {
            // Update
            eventReviewService.updateReview(currentUserReview.getId(), new UpdateEventReviewRequest(rating))
                    .enqueue(new Callback<>() {
                        @Override
                        public void onResponse(@NonNull Call<GetEventReviewResponse> call, @NonNull Response<GetEventReviewResponse> response) {
                            if (response.isSuccessful()) {
                                currentUserReview = response.body();
                                Toast.makeText(requireContext(), getString(R.string.review_updated), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<GetEventReviewResponse> call, @NonNull Throwable t) {
                            Toast.makeText(requireContext(), getString(R.string.error_updating_review), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Create
            eventReviewService.createReview(new CreateEventReviewRequest(rating, eventId))
                    .enqueue(new Callback<>() {
                        @Override
                        public void onResponse(@NonNull Call<GetEventReviewResponse> call, @NonNull Response<GetEventReviewResponse> response) {
                            if (response.isSuccessful()) {
                                currentUserReview = response.body();
                                deleteReviewButton.setVisibility(View.VISIBLE);
                                submitReviewButton.setText(R.string.update_review);
                                Toast.makeText(requireContext(), getString(R.string.review_created), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<GetEventReviewResponse> call, @NonNull Throwable t) {
                            Toast.makeText(requireContext(), R.string.error_creating_review, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void deleteReview() {
        if (currentUserReview == null) return;

        eventReviewService.deleteReview(currentUserReview.getId()).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    currentUserReview = null;
                    ratingBar.setRating(0);
                    deleteReviewButton.setVisibility(View.GONE);
                    submitReviewButton.setText(getString(R.string.submit_review));
                    Toast.makeText(requireContext(), R.string.review_deleted, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), R.string.error_deleting_review, Toast.LENGTH_SHORT).show();
            }
        });
    }
}