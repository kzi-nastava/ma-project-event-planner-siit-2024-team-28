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
import android.os.Debug;
import android.os.Environment;
import android.provider.MediaStore;
import android.graphics.Bitmap;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.eventplanner.model.enums.ChatTheme;
import com.eventplanner.model.requests.chats.CreateChatRequest;
import com.eventplanner.model.requests.chats.FindChatRequest;
import com.eventplanner.model.responses.ErrorResponse;
import com.eventplanner.model.responses.chats.FindChatResponse;
import com.eventplanner.services.ChatService;
import com.eventplanner.utils.Base64Util;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventFragment extends Fragment {
    private FragmentEventBinding binding;
    private EventService eventService;
    private EventTypeService eventTypeService;
    private ChatService chatService;
    private NavController navController;
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
    private DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
    private boolean isPrivate;
    private MapView mapView;
    private Marker locationMarker;
    private double locationLat = 44.8125; // Default Belgrade
    private double locationLng = 20.4612;

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
        chatService = HttpUtils.getChatService();
        navController = Navigation.findNavController(getActivity(), R.id.fragment_nav_content_main);

        // Get event ID from arguments if editing
        if (getArguments() != null) {
            eventId = getArguments().getLong("eventId");
        }

        if (eventId != null && eventId != -1) {
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
        binding.downloadReviewsButton.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
        binding.chatWithOrganizerButton.setVisibility(AuthUtils.getUserId(getContext()).equals(eventOrganizerId) || !isEditMode ? View.GONE : View.VISIBLE);

        eventReviewService = HttpUtils.getEventReviewService();

        LinearLayout reviewSection = binding.getRoot().findViewById(R.id.reviewSection);
        ratingBar = binding.getRoot().findViewById(R.id.ratingBar);
        submitReviewButton = binding.getRoot().findViewById(R.id.submitReviewButton);
        deleteReviewButton = binding.getRoot().findViewById(R.id.deleteReviewButton);

        // Setup review section visibility and functionality
        if (isEditMode) {
            setupReviewSection(reviewSection);
        } else {
            // Creating new event: hide review UI and favorite toggle
            reviewSection.setVisibility(View.GONE);
            submitReviewButton.setVisibility(View.GONE);
            deleteReviewButton.setVisibility(View.GONE);
            binding.favoriteButton.setVisibility(View.GONE);
        }


        mapView = binding.locationMap;
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.setOnTouchListener((v, event) -> {
            // Request parent not to intercept touch events while interacting with the map
            v.getParent().requestDisallowInterceptTouchEvent(true);

            // Allow MapView to handle touch as usual
            return false;
        });


// Restore coordinates if location passed
        if (location != null) {
            locationLat = location.getLatitude();
            locationLng = location.getLongitude();
        }

// Center map
        GeoPoint startPoint = new GeoPoint(locationLat, locationLng);
        mapView.getController().setZoom(13.0);
        mapView.getController().setCenter(startPoint);

// Add at location
        locationMarker = new Marker(mapView);
        locationMarker.setPosition(startPoint);
        locationMarker.setTitle(location != null ? location.getName() : "Location");
        mapView.getOverlays().add(locationMarker);

        }

    private void updateLocationOnMap() {
        if (location != null && mapView != null) {
            locationLat = location.getLatitude();
            locationLng = location.getLongitude();

            GeoPoint newPoint = new GeoPoint(locationLat, locationLng);

            // Update or create marker
            if (locationMarker == null) {
                locationMarker = new Marker(mapView);
                mapView.getOverlays().add(locationMarker);
            }
            locationMarker.setPosition(newPoint);
            locationMarker.setTitle(location.getName());

            // Center the map on new location
            mapView.getController().setZoom(13.0);
            mapView.getController().setCenter(newPoint);

        }
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


    private void setupForm() {
        // Disable form if user is not organizer (for edit mode)
        if (isEditMode && !isEventOrganizerAndCreator()) {
            disableForm();
        }
    }

    private boolean isEventOrganizerAndCreator() {
        Long currentUserId = AuthUtils.getUserId(getContext());
        return currentUserId != null && currentUserId.equals(eventOrganizerId);
    }

    private void setupReviewSection(LinearLayout reviewSection) {
        Long currentUserId = AuthUtils.getUserId(getContext());
        boolean isLoggedIn = currentUserId != null;
        boolean isEventCreator = isEventOrganizerAndCreator();

        if (isLoggedIn && !isEventCreator) {
            // Show review section only for logged-in users who are NOT the event creator
            submitReviewButton.setOnClickListener(v -> submitReview());
            deleteReviewButton.setOnClickListener(v -> deleteReview());

            if (isEditMode) {
                reviewSection.setVisibility(View.VISIBLE);
                loadUserReview(currentUserId, eventId);
            }
        } else {
            // Hide review section for non-logged-in users or event creators
            reviewSection.setVisibility(View.GONE);
            submitReviewButton.setVisibility(View.GONE);
            deleteReviewButton.setVisibility(View.GONE);
        }
    }

    private void setupFavoriteButton() {
        Long currentUserId = AuthUtils.getUserId(getContext());
        boolean isLoggedIn = currentUserId != null;
        boolean isEventCreator = isEventOrganizerAndCreator();

        if (!isLoggedIn || isEventCreator) {
            // Hide favorite button if user is not logged in or if they are the event creator
            binding.favoriteButton.setVisibility(View.GONE);
        } else {
            // Show and enable favorite button for logged in users who are not the event creator
            binding.favoriteButton.setVisibility(View.VISIBLE);
            binding.favoriteButton.setEnabled(true);
        }
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

        binding.imageUploadButton.setEnabled(false);
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
                    isPrivate = event.getPrivacyType().equals("PRIVATE");
                    updateInvitationVisibility();

                    // Check if current user is organizer
                    if (!isEventOrganizerAndCreator()) {
                        disableForm();
                    }

                    populateForm(event);
                    setupButtons();
                    // Now that organizer info is known, set up review section visibility
                    LinearLayout reviewSection = binding.getRoot().findViewById(R.id.reviewSection);
                    setupReviewSection(reviewSection);
                } else {
                    Toast.makeText(requireContext(), getString(R.string.failed_to_load_event), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<GetEventResponse> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), getString(R.string.error_with_message, t.getMessage()), Toast.LENGTH_SHORT).show();
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
            binding.getRoot().post(() -> binding.eventType.setText(getString(R.string.all), false));
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
                        activity.getLocation(),
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
                    eventTypes = new ArrayList<>(response.body());
                    setupEventTypeSpinner();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Collection<GetEventTypeResponse>> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), getString(R.string.failed_to_load_event_types), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupEventTypeSpinner() {
        List<String> typeNames = new ArrayList<>();
        typeNames.add(requireContext().getString(R.string.all)); // First option is always "All"
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
            binding.eventType.setText(getString(R.string.all), false);
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
        binding.downloadReviewsButton.setOnClickListener(v -> downloadEventReviews());
        binding.chatWithOrganizerButton.setOnClickListener(v -> chatWithOrganizer());
        binding.favoriteButton.setOnClickListener(v -> toggleFavorite());
        binding.startDate.setOnClickListener(v -> showDatePicker(binding.startDate));
        binding.endDate.setOnClickListener(v -> showDatePicker(binding.endDate));
        binding.startDate.setFocusable(false);
        binding.startDate.setClickable(true);
        binding.endDate.setFocusable(false);
        binding.endDate.setClickable(true);

        boolean showSubmit = !isEditMode || isEventOrganizerAndCreator();
        boolean showDelete = isEditMode && isEventOrganizerAndCreator();
        binding.submitButton.setVisibility(showSubmit ? View.VISIBLE : View.GONE);
        binding.deleteButton.setVisibility(showDelete ? View.VISIBLE : View.GONE);

        // Show download buttons if in edit mode (regardless of organizer status)
        binding.downloadGuestListButton.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
        binding.downloadDetailsButton.setVisibility(isEditMode ? View.VISIBLE : View.GONE);

        updateInvitationVisibility();
        binding.emailInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.sendButton.setEnabled(isValidEmail(s.toString()));
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        binding.sendButton.setOnClickListener(v -> sendEventInvitation());

        // Setup favorite button visibility and functionality
        setupFavoriteButton();

        // Disable image upload if not organizer
        if (isEditMode && !isEventOrganizerAndCreator()) {
            binding.imageUploadButton.setEnabled(false);
        }
    }

    private void updateInvitationVisibility()
    {
        String TAG = "PrivacyDebug";
        Log.i(TAG, "isEditMode:" + isEditMode);
        Log.i(TAG, "eventId:" + eventId);
        Log.i(TAG, "isPrivate:" + isPrivate);

        binding.invitationTitle.setVisibility((isEditMode && eventId!=null && isPrivate) ? View.VISIBLE : View.GONE);
        binding.sendButton.setVisibility((isEditMode && eventId!=null && isPrivate) ? View.VISIBLE : View.GONE);
        binding.emailInput.setVisibility((isEditMode && eventId!=null && isPrivate) ? View.VISIBLE : View.GONE);
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void openImagePicker() {
        imagePickerLauncher.launch("image/*");
    }

    private void openLocationDialog() {
        boolean isReadOnly = isEditMode && !isEventOrganizerAndCreator();
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
        LocalDate startDate, endDate;
        try {
            startDate = LocalDate.parse(binding.startDate.getText().toString(), formatter);
            endDate = LocalDate.parse(binding.endDate.getText().toString(), formatter);
        } catch (DateTimeParseException e) {
            Toast.makeText(requireContext(), getString(R.string.invalid_date_format_use_yyyy_mm_dd), Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isReadOnly = isEditMode && !isEventOrganizerAndCreator();
        ActivitiesDialogFragment dialog = ActivitiesDialogFragment.newInstance(activities, isReadOnly);

        Bundle args = dialog.getArguments();
        if (args != null) {
            args.putSerializable("eventStartDate", startDate.atStartOfDay());
            args.putSerializable("eventEndDate", endDate.atTime(LocalTime.MAX));
        }
        dialog.show(getChildFragmentManager(), "ActivitiesDialogFragment");

        getChildFragmentManager().setFragmentResultListener("activities_request", this, (requestKey, result) -> {
            String activitiesJson = result.getString("activities");
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(java.time.LocalDateTime.class, new com.eventplanner.adapters.typeAdapters.LocalDateTimeAdapter())
                    .create();
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

        LocalDate startDate, endDate;
        try {
            startDate = LocalDate.parse(binding.startDate.getText().toString(), formatter);
            endDate = LocalDate.parse(binding.endDate.getText().toString(), formatter);
        } catch (DateTimeParseException e) {
            Toast.makeText(requireContext(), getString(R.string.invalid_date_format_use_yyyy_mm_dd), Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate activity times to prevent null LocalDateTime values in request
        for (CreateActivityRequest a : activities) {
            if (a.getStartTime() == null || a.getEndTime() == null) {
                Toast.makeText(requireContext(), getString(R.string.each_activity_must_have_both_start_and_end_time), Toast.LENGTH_SHORT).show();
                return;
            }
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
            Toast.makeText(requireContext(), getString(R.string.name_is_required), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (binding.description.getText().toString().isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.description_is_required), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (binding.maxParticipants.getText().toString().isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.max_participants_is_required), Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            LocalDate startDate = LocalDate.parse(binding.startDate.getText().toString());
            LocalDate endDate = LocalDate.parse(binding.endDate.getText().toString());

            if (endDate.isBefore(startDate)) {
                Toast.makeText(requireContext(), getString(R.string.end_date_must_be_after_start_date), Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (DateTimeParseException e) {
            Toast.makeText(requireContext(), getString(R.string.invalid_date_format), Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private Long getSelectedEventTypeId() {
        String selectedName = binding.eventType.getText().toString();
        if (selectedName.equals(getString(R.string.all))) {
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
                    Toast.makeText(requireContext(), getString(R.string.event_created_successfully), Toast.LENGTH_SHORT).show();
                    navigateBack();
                } else {
                    Toast.makeText(requireContext(), getString(R.string.failed_to_create_event), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), getString(R.string.error_with_message, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEvent(UpdateEventRequest request) {
        eventService.updateEvent(eventId, request).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), getString(R.string.event_updated_successfully), Toast.LENGTH_SHORT).show();
                    navigateBack();
                } else {
                    Toast.makeText(requireContext(), getString(R.string.failed_to_update_event), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), getString(R.string.error_with_message, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDelete() {
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.delete_event_title))
                .setMessage(getString(R.string.delete_event_message))
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteEvent())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void deleteEvent() {
        eventService.deleteEventById(eventId).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), getString(R.string.event_deleted_successfully), Toast.LENGTH_SHORT).show();
                    navigateBack();
                } else {
                    Toast.makeText(requireContext(), getString(R.string.failed_to_delete_event), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), getString(R.string.error_with_message, t.getMessage()), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(requireContext(), getString(R.string.failed_to_download_guest_list), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), getString(R.string.error_with_message, t.getMessage()), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(requireContext(), getString(R.string.failed_to_download_event_details), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), getString(R.string.error_with_message, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void downloadEventReviews() {
        eventService.getEventReviewsReport(eventId).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Save PDF file
                    savePdfFile(response.body(), "event_" + eventId + "_reviews.pdf");
                } else {
                    Toast.makeText(requireContext(), getString(R.string.failed_to_download_event_reviews), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), getString(R.string.error_with_message, t.getMessage()), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(requireContext(), getString(R.string.failed_to_create_file), Toast.LENGTH_LONG).show();
                    return;
                }

                outputStream = resolver.openOutputStream(fileUri);
                if (outputStream == null) {
                    Toast.makeText(requireContext(), getString(R.string.failed_to_open_file), Toast.LENGTH_LONG).show();
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

            Toast.makeText(requireContext(), getString(R.string.pdf_saved_to_downloads), Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            Toast.makeText(requireContext(), getString(R.string.error_saving_file_with_message, e.getMessage()), Toast.LENGTH_LONG).show();
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
                    String message = isFavorite ? getString(R.string.added_to_favorites) : getString(R.string.removed_from_favorites);
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), getString(R.string.failed_to_update_favorite_status), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateFavoriteButton() {
        binding.favoriteButton.setText(isFavorite ? getString(R.string.unfavorite) : getString(R.string.favorite));
    }

    private void updateLocationSummary() {
        if (location != null) {
            binding.locationSummary.setVisibility(View.VISIBLE);
            binding.locationName.setText(location.getName());
            binding.locationAddress.setText(location.getAddress());
        } else {
            binding.locationSummary.setVisibility(View.GONE);
        }
        updateLocationOnMap();
    }

    private void updateActivitiesSummary() {
        if (!activities.isEmpty()) {
            binding.activitiesList.removeAllViews();
            // Formatter to display date and time without seconds
            DateTimeFormatter previewFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            for (int i = 0; i < activities.size(); i++) {
                CreateActivityRequest activity = activities.get(i);
                final int index = i;

                View activityView = LayoutInflater.from(requireContext())
                        .inflate(R.layout.item_activity_preview, binding.activitiesList, false);

                TextView nameView = activityView.findViewById(R.id.activity_name);
                TextView descView = activityView.findViewById(R.id.activity_description);
                TextView locView = activityView.findViewById(R.id.activity_location);
                TextView startView = activityView.findViewById(R.id.activity_start_time);
                TextView endView = activityView.findViewById(R.id.activity_end_time);
                Button removeButton = activityView.findViewById(R.id.remove_activity_button);

                nameView.setText(activity.getName());
                descView.setText(activity.getDescription());
                locView.setText(activity.getLocation());

                // Set times if available, formatted as yyyy-MM-dd HH:mm (no seconds)
                try {
                    if (activity.getStartTime() != null) {
                        startView.setText(activity.getStartTime().format(previewFormatter));
                    } else {
                        startView.setText("");
                    }
                } catch (Exception e) {
                    startView.setText("");
                }
                try {
                    if (activity.getEndTime() != null) {
                        endView.setText(activity.getEndTime().format(previewFormatter));
                    } else {
                        endView.setText("");
                    }
                } catch (Exception e) {
                    endView.setText("");
                }

                // Configure remove button based on permissions
                boolean canEdit = !isEditMode || isEventOrganizerAndCreator();
                if (canEdit) {
                    removeButton.setVisibility(View.VISIBLE);
                    removeButton.setOnClickListener(v -> {
                        if (index >= 0 && index < activities.size()) {
                            activities.remove(index);
                            updateActivitiesSummary();
                        }
                    });
                } else {
                    removeButton.setVisibility(View.GONE);
                }

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

    private void chatWithOrganizer() {
        if(AuthUtils.getUserId(getContext()).equals(eventOrganizerId))
            return;

        findChat();
    }

    private void findChat() {
        FindChatRequest request = new FindChatRequest(AuthUtils.getUserId(getContext()), eventOrganizerId, ChatTheme.EVENT, eventId);
        Call<FindChatResponse> call = chatService.getChatByParticipantsAndTheme(request);

        call.enqueue(new Callback<FindChatResponse>() {
            @Override
            public void onResponse(Call<FindChatResponse> call, Response<FindChatResponse> response) {
                if (response.isSuccessful()) {
                    FindChatResponse result = response.body();
                    if(result != null && result.isFound()) {
                        navigateToChat(result.getChat().getId());
                    }
                    else {
                        createChat();
                    }
                } else {
                    try {
                        String errorJson = response.errorBody().string();
                        ErrorResponse errorResponse = new Gson().fromJson(errorJson, ErrorResponse.class);
                        Toast.makeText(getContext(), errorResponse.getError(), Toast.LENGTH_SHORT).show();
                        Log.e("EventFragment", "Find chat failed: " + errorResponse.getError());
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Find chat failed: " + response.code(),
                                Toast.LENGTH_SHORT).show();
                        Log.e("EventFragment", "Find chat failed", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<FindChatResponse> call, Throwable t) {
                Log.e("EventFragment", "Network failure: " + t.getMessage());
                Toast.makeText(getContext(), "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createChat() {
        // Creating request
        CreateChatRequest request = new CreateChatRequest.Builder()
                .participant1Id(AuthUtils.getUserId(getContext()))
                .participant2Id(eventOrganizerId)
                .theme(ChatTheme.EVENT)
                .themeId(eventId)
                .build();

        Call<Long> call = chatService.createChat(request);

        call.enqueue(new Callback<Long>() {
            @Override
            public void onResponse(Call<Long> call, Response<Long> response) {
                if (response.isSuccessful()) {
                    Long chatId = response.body();
                    Toast.makeText(getContext(), "Chat created with ID: " + chatId, Toast.LENGTH_SHORT).show();
                    Log.e("EventFragment", "Chat created with ID: " + chatId);
                    navigateToChat(chatId);
                } else {
                    try {
                        String errorJson = response.errorBody().string();
                        ErrorResponse errorResponse = new Gson().fromJson(errorJson, ErrorResponse.class);
                        Toast.makeText(getContext(), errorResponse.getError(), Toast.LENGTH_SHORT).show();
                        Log.e("EventFragment", "Create failed: " + errorResponse.getError());
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Create failed: Unknown error", Toast.LENGTH_SHORT).show();
                        Log.e("EventFragment", "Create failed: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(Call<Long> call, Throwable t) {
                Log.e("ChatFragment", "Network failure: " + t.getMessage());
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToChat(Long chatId) {
        Bundle bundle = new Bundle();
        bundle.putLong("chatId", chatId);
        navController.navigate(R.id.action_event_to_chat, bundle);
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

    private void sendEventInvitation()
    {
        String email = binding.emailInput.getText().toString();
        if (email.isEmpty() || eventId == 0) return;

        eventService.createEventInvitation(eventId, email)
                .enqueue(new Callback<Long>() {
                    @Override
                    public void onResponse(@NonNull Call<Long> call, @NonNull Response<Long> response) {
                        if (response.isSuccessful()) {
                            binding.emailInput.setText("");
                            Toast.makeText(requireContext(), getString(R.string.email_sent), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Long> call, @NonNull Throwable t) {
                        Toast.makeText(requireContext(), R.string.error_creating_review, Toast.LENGTH_SHORT).show();
                    }
                });
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

    private final ActivityResultLauncher<String> imagePickerLauncher =
        registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                Bitmap bitmap = Base64Util.getBitmapFromUri(requireContext(), uri);
                if (bitmap != null) {
                    imageBase64 = Base64Util.encodeImageToBase64(bitmap);
                    binding.eventImage.setImageBitmap(bitmap);
                } else {
                    Toast.makeText(requireContext(), getString(R.string.failed_to_load_image), Toast.LENGTH_SHORT).show();
                }
            }
        });
}