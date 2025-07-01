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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.eventplanner.R;
import com.eventplanner.activities.HomeActivity;
import com.eventplanner.model.constants.UserRoles;
import com.eventplanner.model.requests.auth.UpdateBusinessOwnerRequest;
import com.eventplanner.model.requests.auth.UpdateEventOrganizerRequest;
import com.eventplanner.model.responses.users.GetUserProfilePictureResponse;
import com.eventplanner.model.responses.users.GetUserResponse;
import com.eventplanner.utils.AuthUtils;
import com.eventplanner.utils.Base64Util;
import com.eventplanner.utils.FormValidator;
import com.eventplanner.utils.HttpUtils;
import com.google.android.material.textfield.TextInputLayout;

import java.io.InputStream;
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

        // Get current user info
        userId = AuthUtils.getUserId(requireContext());
        currentUserRoles = AuthUtils.getUserRoles(requireContext());
        checkDeactivationStatus();

        // Setup role-based visibility
        setupRoleBasedViews(view);

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

        // Load user data
        loadUserData();

        return view;
    }

    private void setupRoleBasedViews(View view) {
        if (currentUserRoles == null) {
            return;
        }

        boolean isBusinessOwner = false;
        boolean isEventOrganizer = false;

        for (String role : currentUserRoles) {
            if (role.equals(UserRoles.BusinessOwner)) {
                isBusinessOwner = true;
            } else if (role.equals(UserRoles.EventOrganizer)) {
                isEventOrganizer = true;
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
        }
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
                    if (!base64Image.isEmpty()) {
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
        }

        String addressStr = address.getText().toString().trim();
        if (FormValidator.isEmpty(addressStr)) {
            address.setError(getString(R.string.error_address_required));
            valid = false;
        }

        if (businessNameLayout.getVisibility() == View.VISIBLE) {
            String businessNameStr = businessName.getText().toString().trim();
            if (FormValidator.isEmpty(businessNameStr)) {
                businessName.setError(getString(R.string.error_organization_name_required));
                valid = false;
            }

            String businessDescStr = businessDescription.getText().toString().trim();
            if (FormValidator.isEmpty(businessDescStr)) {
                businessDescription.setError(getString(R.string.error_description_required));
                valid = false;
            }
        }

        if (firstNameLayout.getVisibility() == View.VISIBLE) {
            String firstNameStr = firstName.getText().toString().trim();
            if (FormValidator.isEmpty(firstNameStr)) {
                firstName.setError(getString(R.string.error_first_name_required));
                valid = false;
            }

            String lastNameStr = lastName.getText().toString().trim();
            if (FormValidator.isEmpty(lastNameStr)) {
                lastName.setError(getString(R.string.error_last_name_required));
                valid = false;
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
                email.getText().toString(),
                phoneNumber.getText().toString(),
                profilePictureBase64,
                address.getText().toString(),
                businessDescription.getText().toString(),
                businessName.getText().toString()
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
}
