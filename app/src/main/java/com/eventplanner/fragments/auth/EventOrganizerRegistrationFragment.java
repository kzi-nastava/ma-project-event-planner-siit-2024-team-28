package com.eventplanner.fragments.auth;

import android.app.Activity;
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
import com.eventplanner.model.requests.auth.RegisterEventOrganizerRequest;
import com.eventplanner.utils.Base64Util;
import com.eventplanner.utils.FormValidator;
import com.eventplanner.utils.HttpUtils;

import java.io.InputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventOrganizerRegistrationFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1001;

    private EditText email, password, repeatPassword, lastName, firstName, address, phoneNumber;
    private ImageView profilePicturePreview;
    private String profilePictureBase64 = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_organizer_registration, container, false);

        email = view.findViewById(R.id.email);
        password = view.findViewById(R.id.password);
        repeatPassword = view.findViewById(R.id.repeatPassword);
        lastName = view.findViewById(R.id.lastName);
        firstName = view.findViewById(R.id.firstName);
        address = view.findViewById(R.id.address);
        phoneNumber = view.findViewById(R.id.phoneNumber);

        profilePicturePreview = view.findViewById(R.id.profilePicturePreview);
        Button uploadProfilePictureButton = view.findViewById(R.id.uploadProfilePictureButton);

        Button registerButton = view.findViewById(R.id.submitButton);

        uploadProfilePictureButton.setOnClickListener(v -> openImagePicker());

        registerButton.setOnClickListener(v -> {
            if (validateForm()) {
                registerEventOrganizer();
            }
        });

        return view;
    }

    private boolean validateForm() {
        boolean valid = true;

        String emailStr = email.getText().toString().trim();
        if (FormValidator.isEmpty(emailStr)) {
            email.setError(getString(R.string.error_email_required));
            valid = false;
        } else if (!FormValidator.isValidEmail(emailStr)) {
            email.setError(getString(R.string.error_email_invalid));
            valid = false;
        } else {
            email.setError(null);
        }

        String passwordStr = password.getText().toString();
        if (FormValidator.isEmpty(passwordStr)) {
            password.setError(getString(R.string.error_password_required));
            valid = false;
        } else if (!FormValidator.isValidPassword(passwordStr)) {
            password.setError(getString(R.string.error_password_invalid));
            valid = false;
        } else {
            password.setError(null);
        }

        String repeatPasswordStr = repeatPassword.getText().toString();
        if (FormValidator.isEmpty(repeatPasswordStr)) {
            repeatPassword.setError(getString(R.string.error_repeat_password_required));
            valid = false;
        } else if (!repeatPasswordStr.equals(passwordStr)) {
            repeatPassword.setError(getString(R.string.error_passwords_mismatch));
            valid = false;
        } else {
            repeatPassword.setError(null);
        }

        String lastNameStr = lastName.getText().toString().trim();
        if (FormValidator.isEmpty(lastNameStr)) {
            lastName.setError(getString(R.string.error_last_name_required));
            valid = false;
        } else if (!FormValidator.hasMinLength(lastNameStr, 1)) {
            lastName.setError(getString(R.string.error_last_name_invalid));
            valid = false;
        } else {
            lastName.setError(null);
        }

        String firstNameStr = firstName.getText().toString().trim();
        if (FormValidator.isEmpty(firstNameStr)) {
            firstName.setError(getString(R.string.error_first_name_required));
            valid = false;
        } else if (!FormValidator.hasMinLength(firstNameStr, 1)) {
            firstName.setError(getString(R.string.error_first_name_invalid));
            valid = false;
        } else {
            firstName.setError(null);
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

        return valid;
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.registration_select_profile_pic)), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                try {
                    InputStream inputStream = activity.getContentResolver().openInputStream(imageUri);
                    Bitmap selectedImage = BitmapFactory.decodeStream(inputStream);
                    profilePicturePreview.setImageBitmap(selectedImage);

                    profilePictureBase64 = Base64Util.encodeImageToBase64(selectedImage);
                } catch (Exception e) {
                    Toast.makeText(getContext(), getString(R.string.toast_image_upload_fail), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void registerEventOrganizer() {
        RegisterEventOrganizerRequest request = new RegisterEventOrganizerRequest(
                email.getText().toString(),
                password.getText().toString(),
                phoneNumber.getText().toString(),
                profilePictureBase64,
                address.getText().toString(),
                firstName.getText().toString(),
                lastName.getText().toString()
        );

        new Thread(() -> {
            Call<Void> call = HttpUtils.getAuthService().registerEventOrganizer(request);
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    if (response.isSuccessful() && getActivity() != null && getActivity().getApplicationContext() != null) {
                        Toast.makeText(getContext(), getString(R.string.toast_check_email), Toast.LENGTH_SHORT).show();

                        View view = getView();
                        if (view != null) {
                            Navigation.findNavController(view).navigate(R.id.nav_home);
                        }
                    } else {
                        Toast.makeText(getContext(), getString(R.string.toast_registration_error), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    Toast.makeText(getContext(), getString(R.string.toast_registration_failed), Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
}