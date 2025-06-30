package com.eventplanner.fragments.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.eventplanner.R;
import com.eventplanner.model.requests.RegisterEventOrganizerRequest;
import com.eventplanner.model.responses.AuthResponse;
import com.eventplanner.utils.FormValidator;
import com.eventplanner.utils.HttpUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventOrganizerRegistrationFragment extends Fragment {
    private EditText email, password, repeatPassword, lastName, firstName, address, phoneNumber;

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
        Button registerButton = view.findViewById(R.id.submitButton);

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

    private void registerEventOrganizer() {
        RegisterEventOrganizerRequest request = new RegisterEventOrganizerRequest(
                email.getText().toString(),
                password.getText().toString(),
                phoneNumber.getText().toString(),
                null,
                address.getText().toString(),
                firstName.getText().toString(),
                lastName.getText().toString()
        );

        new Thread(() -> {
            Call<AuthResponse> call = HttpUtils.getAuthService().registerEventOrganizer(request);
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                    if (response.isSuccessful() && getActivity() != null && getActivity().getApplicationContext() != null) {
                        Toast.makeText(getContext(), getString(R.string.toast_check_email), Toast.LENGTH_SHORT).show();

                        // Navigate back to HomeFragment
                        View view = getView();
                        if (view != null) {
                            Navigation.findNavController(view).navigate(R.id.nav_home);
                        }
                    } else {
                        Toast.makeText(getContext(), getString(R.string.toast_registration_error), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                    Toast.makeText(getContext(), getString(R.string.toast_registration_failed), Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
}