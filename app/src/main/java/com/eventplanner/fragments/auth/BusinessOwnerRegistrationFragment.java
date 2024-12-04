package com.eventplanner.fragments.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.eventplanner.R;
import com.eventplanner.model.requests.RegisterBusinessOwnerRequest;
import com.eventplanner.model.responses.AuthResponse;
import com.eventplanner.utils.AuthUtils;
import com.eventplanner.utils.ClientUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BusinessOwnerRegistrationFragment extends Fragment {
    private EditText email, password, repeatPassword, organizationName, address, phoneNumber, description;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_business_owner_registration, container, false);

        email = view.findViewById(R.id.email);
        password = view.findViewById(R.id.password);
        repeatPassword = view.findViewById(R.id.repeatPassword);
        organizationName = view.findViewById(R.id.organizationName);
        address = view.findViewById(R.id.address);
        phoneNumber = view.findViewById(R.id.phoneNumber);
        description = view.findViewById(R.id.description);
        Button registerButton = view.findViewById(R.id.submitButton);

        registerButton.setOnClickListener(v -> {
            if (validateForm()) {
                registerBusinessOwner();
            }
        });

        return view;
    }

    private boolean validateForm() {
        if (!isValidEmail(email.getText().toString())) {
            email.setError("Please enter a valid email address");
            return false;
        }
        if (TextUtils.isEmpty(password.getText())) {
            password.setError("Password is required");
            return false;
        }
        if (TextUtils.isEmpty(repeatPassword.getText())) {
            repeatPassword.setError("Please repeat your password");
            return false;
        }
        if (!password.getText().toString().equals(repeatPassword.getText().toString())) {
            repeatPassword.setError("Passwords do not match");
            return false;
        }
        if (TextUtils.isEmpty(organizationName.getText())) {
            organizationName.setError("Organization Name is required");
            return false;
        }
        if (TextUtils.isEmpty(address.getText())) {
            address.setError("Address is required");
            return false;
        }
        if (TextUtils.isEmpty(phoneNumber.getText())) {
            phoneNumber.setError("Phone number is required");
            return false;
        }
        if (TextUtils.isEmpty(description.getText())) {
            description.setError("Description is required");
            return false;
        }
        return true;
    }

    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}";
        return email.matches(emailPattern);
    }

    private void registerBusinessOwner() {
        RegisterBusinessOwnerRequest request = new RegisterBusinessOwnerRequest(
            email.getText().toString(),
            password.getText().toString(),
            phoneNumber.getText().toString(),
            null,
            address.getText().toString(),
            organizationName.getText().toString(),
            description.getText().toString()
        );

        new Thread(() -> {
            Call<AuthResponse> call = ClientUtils.getAuthService().registerBusinessOwner(request);
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getJwtToken() != null && getActivity() != null) {
                        AuthUtils.saveToken(getActivity().getApplicationContext(), response.body().getJwtToken());
                        Toast.makeText(getContext(), "Registration successful!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "An error occurred while registering. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<AuthResponse> call, Throwable t) {
                    Log.d("Backend call", t.getMessage() != null ? t.getMessage() : "unknown error");
                    Toast.makeText(getContext(), "Registration failed. Try again.", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
}