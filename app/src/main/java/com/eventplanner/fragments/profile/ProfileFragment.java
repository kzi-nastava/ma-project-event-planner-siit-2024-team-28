package com.eventplanner.fragments.profile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.eventplanner.R;
import com.eventplanner.activities.HomeActivity;
import com.eventplanner.adapters.events.EventListAdapter;
import com.eventplanner.adapters.solutions.FavoriteSolutionListAdapter;
import com.eventplanner.components.CalendarComponent;
import com.eventplanner.model.constants.UserRoles;
import com.eventplanner.model.responses.calendar.CalendarEventDTO;
import com.eventplanner.model.requests.users.UpdateBusinessOwnerRequest;
import com.eventplanner.model.requests.users.UpdateEventOrganizerRequest;
import com.eventplanner.model.requests.users.UpdateUserRequest;
import com.eventplanner.model.responses.events.GetEventResponse;
import com.eventplanner.model.responses.solutionCateogries.GetSolutionCategoryResponse;
import com.eventplanner.model.responses.solutions.GetSolutionResponse;
import com.eventplanner.model.responses.users.GetUserProfilePictureResponse;
import com.eventplanner.model.responses.users.GetUserResponse;
import com.eventplanner.utils.AuthUtils;
import com.eventplanner.utils.Base64Util;
import com.eventplanner.utils.FormValidator;
import com.eventplanner.utils.HttpUtils;
import com.google.android.material.textfield.TextInputLayout;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1001;
    private EditText email, phoneNumber, address, businessName, businessDescription, firstName, lastName;
    private ImageView profilePicturePreview;
    private String profilePictureBase64 = null;
    private List<String> currentUserRoles;
    private Long userId;
    private TextInputLayout businessNameLayout, businessDescriptionLayout, firstNameLayout, lastNameLayout;
    private ListView favoriteEventsList;
    private TextView favoriteEventsHeader;
    private TextView noFavoriteEventsText;
    private List<GetEventResponse> favoriteEvents = new ArrayList<>();
    private ListView favoriteSolutionsList;
    private ListView ownerCategoriesListView;
    private TextView favoriteSolutionsHeader;
    private TextView noFavoriteSolutionsText;
    private List<GetSolutionResponse> favoriteSolutions = new ArrayList<>();

    private CalendarComponent calendarComponent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize views
        email = view.findViewById(R.id.email);
        phoneNumber = view.findViewById(R.id.phoneNumber);
        address = view.findViewById(R.id.address);
        businessName = view.findViewById(R.id.businessName);
        businessDescription = view.findViewById(R.id.businessDescription);
        firstName = view.findViewById(R.id.firstName);
        lastName = view.findViewById(R.id.lastName);
        profilePicturePreview = view.findViewById(R.id.profilePicturePreview);
        businessNameLayout = view.findViewById(R.id.businessNameLayout);
        businessDescriptionLayout = view.findViewById(R.id.businessDescriptionLayout);
        firstNameLayout = view.findViewById(R.id.firstNameLayout);
        lastNameLayout = view.findViewById(R.id.lastNameLayout);
        favoriteEventsList = view.findViewById(R.id.favoriteEventsList);
        favoriteEventsHeader = view.findViewById(R.id.favoriteEventsHeader);
        noFavoriteEventsText = view.findViewById(R.id.noFavoriteEventsText);

        favoriteSolutionsList = view.findViewById(R.id.favoriteSolutionsList);
        ownerCategoriesListView = view.findViewById(R.id.ownerCategoriesListView);
        favoriteSolutionsHeader = view.findViewById(R.id.favoriteSolutionsHeader);
        noFavoriteSolutionsText = view.findViewById(R.id.noFavoriteSolutionsText);

        // Initialize calendar component
        calendarComponent = view.findViewById(R.id.calendarComponent);
        setupCalendarComponent();

        // Get current user info
        userId = AuthUtils.getUserId(requireContext());
        currentUserRoles = AuthUtils.getUserRoles(requireContext());
        checkDeactivationStatus();

        setupRoleBasedViews();

        // Set click listeners
        Button uploadProfilePictureButton = view.findViewById(R.id.uploadProfilePictureButton);
        uploadProfilePictureButton.setText(getString(R.string.profile_upload_picture));
        uploadProfilePictureButton.setOnClickListener(v -> openImagePicker());

        Button saveButton = view.findViewById(R.id.saveButton);
        saveButton.setText(getString(R.string.profile_save_button));
        saveButton.setOnClickListener(v -> saveProfile());

        Button changePasswordButton = view.findViewById(R.id.changePasswordButton);
        changePasswordButton.setText(getString(R.string.profile_change_password_button));
        changePasswordButton.setOnClickListener(v -> openChangePasswordFragment());

        Button deactivateButton = view.findViewById(R.id.deactivateButton);
        deactivateButton.setText(getString(R.string.profile_deactivate_button));
        deactivateButton.setOnClickListener(v -> deactivateAccount());

        loadUserData();
        loadOwnerCategories();
        loadFavoriteEvents();
        loadFavoriteSolutions();

        return view;
    }

    private void setupRoleBasedViews() {
        if (currentUserRoles == null) {
            return;
        }

        boolean isBusinessOwner = false;
        boolean isEventOrganizer = false;
        boolean isAdmin = false;

        for (String role : currentUserRoles) {
            switch (role) {
                case UserRoles.BusinessOwner:
                    isBusinessOwner = true;
                    break;
                case UserRoles.EventOrganizer:
                    isEventOrganizer = true;
                    break;
                case UserRoles.ADMIN:
                    isAdmin = true;
                    break;
            }
        }

        if (isBusinessOwner) {
            businessNameLayout.setVisibility(View.VISIBLE);
            businessDescriptionLayout.setVisibility(View.VISIBLE);
            firstNameLayout.setVisibility(View.GONE);
            lastNameLayout.setVisibility(View.GONE);
        } else if (isEventOrganizer) {
            businessNameLayout.setVisibility(View.GONE);
            businessDescriptionLayout.setVisibility(View.GONE);
            firstNameLayout.setVisibility(View.VISIBLE);
            lastNameLayout.setVisibility(View.VISIBLE);
        } else if (isAdmin) {
            // Admin users only have basic fields (email, phone, address, profile picture)
            businessNameLayout.setVisibility(View.GONE);
            businessDescriptionLayout.setVisibility(View.GONE);
            firstNameLayout.setVisibility(View.GONE);
            lastNameLayout.setVisibility(View.GONE);
        }

        // Favorite events section is visible for all authenticated users
        favoriteEventsHeader.setVisibility(View.VISIBLE);
        favoriteEventsList.setVisibility(View.VISIBLE);
        // noFavoriteEventsText visibility will be handled in setupFavoriteEventsList()

        // Favorite solutions section is visible for all authenticated users
        favoriteSolutionsHeader.setVisibility(View.VISIBLE);
        favoriteSolutionsList.setVisibility(View.VISIBLE);
        // noFavoriteSolutionsText visibility will be handled in setupFavoriteSolutionsList()
    }

    private void loadUserData() {
        if (userId == null) return;

        // Get user profile
        Call<GetUserResponse> call = HttpUtils.getUserService().getUserById(userId);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<GetUserResponse> call, @NonNull Response<GetUserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GetUserResponse user = response.body();
                    populateForm(user);
                }
            }

            @Override
            public void onFailure(@NonNull Call<GetUserResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), getString(R.string.toast_registration_failed), Toast.LENGTH_SHORT).show();
            }
        });

        // Get profile picture
        Call<GetUserProfilePictureResponse> pictureCall = HttpUtils.getUserService().getUserProfilePictureBase64(userId);
        pictureCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<GetUserProfilePictureResponse> call, @NonNull Response<GetUserProfilePictureResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String base64Image = response.body().getProfilePictureBase64();
                    if (base64Image != null && !base64Image.isEmpty()) {
                        profilePictureBase64 = base64Image;
                        Bitmap bitmap = Base64Util.decodeBase64ToBitmap(base64Image);
                        if (bitmap != null) {
                            profilePicturePreview.setImageBitmap(bitmap);
                        } else {
                            // Set a default image if decoding fails
                            profilePicturePreview.setImageResource(R.drawable.user);
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<GetUserProfilePictureResponse> call, @NonNull Throwable t) {
                // Set default image on failure
                profilePicturePreview.setImageResource(R.drawable.user);
            }
        });
    }

    private void populateForm(GetUserResponse user) {
        email.setText(user.getEmail());
        phoneNumber.setText(user.getPhoneNumber());
        address.setText(user.getAddress());

        if (businessName.getVisibility() == View.VISIBLE) {
            businessName.setText(user.getBusinessName());
            businessDescription.setText(user.getBusinessDescription());
        }

        if (firstName.getVisibility() == View.VISIBLE) {
            firstName.setText(user.getFirstName());
            lastName.setText(user.getLastName());
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.profile_select_picture)), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                try {
                    InputStream inputStream = requireActivity().getContentResolver().openInputStream(imageUri);
                    Bitmap selectedImage = BitmapFactory.decodeStream(inputStream);
                    profilePicturePreview.setImageBitmap(selectedImage);
                    profilePictureBase64 = Base64Util.encodeImageToBase64(selectedImage);
                } catch (Exception e) {
                    Toast.makeText(getContext(), getString(R.string.toast_image_upload_fail), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private boolean validateForm() {
        boolean valid = true;

        String phoneStr = phoneNumber.getText().toString().trim();
        if (FormValidator.isEmpty(phoneStr)) {
            phoneNumber.setError(getString(R.string.error_phone_required));
            valid = false;
        } else if (!FormValidator.isValidPhoneNumber(phoneStr)) {
            phoneNumber.setError(getString(R.string.error_phone_invalid));
            valid = false;
        } else {
            phoneNumber.setError(null);
        }

        String addressStr = address.getText().toString().trim();
        if (FormValidator.isEmpty(addressStr)) {
            address.setError(getString(R.string.error_address_required));
            valid = false;
        } else if (!FormValidator.isValidAddress(addressStr)) {
            address.setError(getString(R.string.error_address_invalid));
            valid = false;
        } else {
            address.setError(null);
        }

        if (businessNameLayout.getVisibility() == View.VISIBLE) {
            String businessDescStr = businessDescription.getText().toString().trim();
            if (FormValidator.isEmpty(businessDescStr)) {
                businessDescription.setError(getString(R.string.error_description_required));
                valid = false;
            } else {
                businessDescription.setError(null);
            }
        }

        if (firstNameLayout.getVisibility() == View.VISIBLE) {
            String firstNameStr = firstName.getText().toString().trim();
            if (FormValidator.isEmpty(firstNameStr)) {
                firstName.setError(getString(R.string.error_first_name_required));
                valid = false;
            } else {
                firstName.setError(null);
            }

            String lastNameStr = lastName.getText().toString().trim();
            if (FormValidator.isEmpty(lastNameStr)) {
                lastName.setError(getString(R.string.error_last_name_required));
                valid = false;
            } else {
                lastName.setError(null);
            }
        }

        return valid;
    }

    private void saveProfile() {
        if (!validateForm() || userId == null) {
            return;
        }

        if (isBusinessOwner()) {
            updateBusinessOwner();
        } else if (isEventOrganizer()) {
            updateEventOrganizer();
        } else {
            updateUser();
        }
    }

    private boolean isBusinessOwner() {
        if (currentUserRoles == null) {
            return false;
        }

        for (String role : currentUserRoles) {
            if (role.equals(UserRoles.BusinessOwner)) return true;
        }
        return false;
    }

    private boolean isEventOrganizer() {
        if (currentUserRoles == null) {
            return false;
        }

        for (String role : currentUserRoles) {
            if (role.equals(UserRoles.EventOrganizer)) return true;
        }
        return false;
    }

    private void updateBusinessOwner() {
        UpdateBusinessOwnerRequest request = new UpdateBusinessOwnerRequest(
                phoneNumber.getText().toString(),
                profilePictureBase64,
                address.getText().toString(),
                businessDescription.getText().toString()
        );

        Call<Void> call = HttpUtils.getUserService().updateBusinessOwner(userId, request);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), getString(R.string.toast_profile_updated_success), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), getString(R.string.toast_profile_update_failed), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), getString(R.string.toast_network_error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEventOrganizer() {
        UpdateEventOrganizerRequest request = new UpdateEventOrganizerRequest(
                phoneNumber.getText().toString(),
                profilePictureBase64,
                address.getText().toString(),
                firstName.getText().toString(),
                lastName.getText().toString()
        );

        Call<Void> call = HttpUtils.getUserService().updateEventOrganizer(userId, request);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), getString(R.string.toast_profile_updated_success), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), getString(R.string.toast_profile_update_failed), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), getString(R.string.toast_network_error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUser() {
        UpdateUserRequest request = new UpdateUserRequest(
                phoneNumber.getText().toString(),
                profilePictureBase64,
                address.getText().toString()
        );

        Call<Void> call = HttpUtils.getUserService().updateUser(userId, request);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), getString(R.string.toast_profile_updated_success), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), getString(R.string.toast_profile_update_failed), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), getString(R.string.toast_network_error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openChangePasswordFragment() {
        Navigation.findNavController(requireView()).navigate(R.id.nav_change_password);
    }

    private void checkDeactivationStatus() {
        if (userId == null) {
            return;
        }

        Call<Boolean> call = HttpUtils.getUserService().canUserBeDeactivated(userId);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Boolean> call, @NonNull Response<Boolean> response) {
                if (response.isSuccessful() && response.body() != null) {
                    View view = getView();

                    if (view == null) {
                        return;
                    }

                    boolean canDeactivate = response.body();
                    Button deactivateButton = view.findViewById(R.id.deactivateButton);
                    deactivateButton.setEnabled(canDeactivate);

                    if (!canDeactivate) {
                        deactivateButton.setAlpha(0.5f); // Visual indication that button is disabled
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Boolean> call, @NonNull Throwable t) {
                // Handle failure silently or show a message
            }
        });
    }

    private void deactivateAccount() {
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.profile_deactivate_title))
                .setMessage(getString(R.string.profile_deactivate_confirmation))
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> performDeactivation())
                .setNegativeButton(getString(R.string.no), null)
                .show();
    }

    private void performDeactivation() {
        Call<Void> call = HttpUtils.getUserService().deactivateCurrentUserAccount();
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    // Logout and redirect to login/start screen
                    AuthUtils.clearToken(requireContext());

                    // Update navigation menu after deactivation
                    if (getActivity() instanceof HomeActivity) {
                        ((HomeActivity) getActivity()).updateNavMenu();
                    }

                    // Navigate back to HomeFragment
                    View view = getView();
                    if (view != null) {
                        Navigation.findNavController(view).navigate(R.id.nav_home);
                    }
                } else {
                    Toast.makeText(getContext(),
                            getString(R.string.toast_deactivation_failed),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(getContext(),
                        getString(R.string.toast_network_error),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFavoriteEvents() {
        Call<Collection<GetEventResponse>> call = HttpUtils.getEventService().getFavoriteEventsForCurrentUser();
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Collection<GetEventResponse>> call, @NonNull Response<Collection<GetEventResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    favoriteEvents = new ArrayList<>(response.body());
                } else {
                    favoriteEvents = new ArrayList<>();
                }
                setupFavoriteEventsList();
            }

            @Override
            public void onFailure(@NonNull Call<Collection<GetEventResponse>> call, @NonNull Throwable t) {
                // Handle error by showing empty list
                favoriteEvents = new ArrayList<>();
                setupFavoriteEventsList();
            }
        });
    }

    private void setupFavoriteEventsList() {
        if (favoriteEvents.isEmpty()) {
            // Show "no favorite events" message and hide the list
            noFavoriteEventsText.setVisibility(View.VISIBLE);
            favoriteEventsList.setVisibility(View.GONE);
        } else {
            // Show the list and hide the "no favorite events" message
            noFavoriteEventsText.setVisibility(View.GONE);
            favoriteEventsList.setVisibility(View.VISIBLE);

            EventListAdapter adapter = new EventListAdapter(requireContext(), favoriteEvents);
            favoriteEventsList.setAdapter(adapter);

            favoriteEventsList.setOnItemClickListener((parent, view, position, id) -> {
                GetEventResponse event = favoriteEvents.get(position);
                navigateToEventDetails(event.getId());
            });
        }
    }

    private void loadFavoriteSolutions() {
        Call<Collection<GetSolutionResponse>> call = HttpUtils.getSolutionService().getFavoriteSolutionsForCurrentUser();
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Collection<GetSolutionResponse>> call, @NonNull Response<Collection<GetSolutionResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    favoriteSolutions = new ArrayList<>(response.body());
                } else {
                    favoriteSolutions = new ArrayList<>();
                }
                setupFavoriteSolutionsList();
            }

            @Override
            public void onFailure(@NonNull Call<Collection<GetSolutionResponse>> call, @NonNull Throwable t) {
                // Handle error by showing empty list
                favoriteSolutions = new ArrayList<>();
                setupFavoriteSolutionsList();
            }
        });
    }

    private void setupFavoriteSolutionsList() {
        if (favoriteSolutions.isEmpty()) {
            // Show "no favorite solutions" message and hide the list
            noFavoriteSolutionsText.setVisibility(View.VISIBLE);
            favoriteSolutionsList.setVisibility(View.GONE);
        } else {
            // Show the list and hide the "no favorite solutions" message
            noFavoriteSolutionsText.setVisibility(View.GONE);
            favoriteSolutionsList.setVisibility(View.VISIBLE);

            FavoriteSolutionListAdapter adapter = new FavoriteSolutionListAdapter(requireContext(), favoriteSolutions);
            favoriteSolutionsList.setAdapter(adapter);

            favoriteSolutionsList.setOnItemClickListener((parent, view, position, id) -> {
                GetSolutionResponse solution = favoriteSolutions.get(position);
                navigateToSolutionDetails(solution.getId());
            });
        }
    }

    private void navigateToSolutionDetails(long solutionId) {
        NavController navController = Navigation.findNavController(requireView());
        Bundle args = new Bundle();
        args.putString("solutionId", String.valueOf(solutionId));
        navController.navigate(R.id.action_profile_to_solution_details, args);
    }

    private void setupCalendarComponent() {
        if (calendarComponent != null) {
            calendarComponent.setOnEventClickListener(this::onCalendarEventClick);
        }
    }

    private void onCalendarEventClick(CalendarEventDTO event) {
        // Navigate based on event type
        switch (event.getType()) {
            case EVENT:
            case CREATED_EVENT:
                // Navigate to event details
                if (event.getRelatedEntityId() != null) {
                    navigateToEventDetails(event.getRelatedEntityId());
                } else {
                    showEventDetailsDialog(event);
                }
                break;
            case SERVICE_RESERVATION:
                // Navigate to service details
                if (event.getRelatedEntityId() != null) {
                    navigateToServiceDetails(event.getRelatedEntityId());
                } else {
                    showEventDetailsDialog(event);
                }
                break;
            default:
                showEventDetailsDialog(event);
                break;
        }
    }

    private void navigateToEventDetails(Long eventId) {
        NavController navController = Navigation.findNavController(requireView());
        Bundle args = new Bundle();
        args.putLong("eventId", eventId);
        navController.navigate(R.id.action_profile_to_event, args);
    }

    private void navigateToServiceDetails(Long serviceId) {
        NavController navController = Navigation.findNavController(requireView());
        Bundle args = new Bundle();
        args.putString("solutionId", String.valueOf(serviceId));
        navController.navigate(R.id.action_profile_to_solution_details, args);
    }

    private void showEventDetailsDialog(CalendarEventDTO event) {
        // Create a dialog to show event details with navigation option
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(event.getTitle());

        StringBuilder message = new StringBuilder();
        if (event.getDescription() != null && !event.getDescription().isEmpty()) {
            message.append("Description: ").append(event.getDescription()).append("\n\n");
        }

        message.append("Date: ").append(event.getStartDate());
        if (event.getEndDate() != null && !event.getEndDate().equals(event.getStartDate())) {
            message.append(" - ").append(event.getEndDate());
        }
        message.append("\n");

        if (event.getStartTime() != null) {
            message.append("Time: ").append(event.getStartTime());
            if (event.getEndTime() != null) {
                message.append(" - ").append(event.getEndTime());
            }
            message.append("\n");
        }

        if (event.getLocation() != null && !event.getLocation().isEmpty()) {
            message.append("Location: ").append(event.getLocation()).append("\n");
        }

        if (event.getStatus() != null && !event.getStatus().isEmpty()) {
            message.append("Status: ").append(event.getStatus()).append("\n");
        }

        message.append("Type: ").append(event.getType().toString());

        builder.setMessage(message.toString());

        // Add navigation buttons if we have a related entity ID
        if (event.getRelatedEntityId() != null) {
            if (event.getType() == com.eventplanner.model.enums.CalendarEventType.EVENT ||
                event.getType() == com.eventplanner.model.enums.CalendarEventType.CREATED_EVENT) {
                builder.setPositiveButton("View Event", (dialog, which) ->
                    navigateToEventDetails(event.getRelatedEntityId()));
            } else if (event.getType() == com.eventplanner.model.enums.CalendarEventType.SERVICE_RESERVATION) {
                builder.setPositiveButton("View Service", (dialog, which) ->
                    navigateToServiceDetails(event.getRelatedEntityId()));
            }
            builder.setNegativeButton("Close", null);
        } else {
            builder.setPositiveButton("OK", null);
        }

        builder.show();
    }

    private void loadOwnerCategories() {
        Call<Collection<GetSolutionCategoryResponse>> call = HttpUtils.getSolutionCategoryService().getCurrentBusinessOwnerCategories();
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Collection<GetSolutionCategoryResponse>> call,
                                   @NonNull Response<Collection<GetSolutionCategoryResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Collection<GetSolutionCategoryResponse> categories = response.body();

                    List<String> items = new ArrayList<>();
                    if (categories.isEmpty()) {
                        items.add("You haven't created any solution categories yet.");
                    } else {
                        for (GetSolutionCategoryResponse c : categories) {
                            items.add(c.getName() + "\n" + c.getDescription());
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_list_item_1,
                            items
                    );
                    ownerCategoriesListView.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Collection<GetSolutionCategoryResponse>> call,
                                  @NonNull Throwable t) {
                Toast.makeText(getContext(), "Failed to load current user's solution categories", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
