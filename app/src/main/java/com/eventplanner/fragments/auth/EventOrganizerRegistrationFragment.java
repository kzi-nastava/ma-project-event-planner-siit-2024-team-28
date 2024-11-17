package com.eventplanner.fragments.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.eventplanner.R;

public class EventOrganizerRegistrationFragment extends Fragment {
    private EditText email, password, repeatPassword, lastName, firstName, address, phoneNumber;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_event_organizer_registration, container, false);

        // Initialize views
        email = view.findViewById(R.id.email);
        password = view.findViewById(R.id.password);
        repeatPassword = view.findViewById(R.id.repeatPassword);
        lastName = view.findViewById(R.id.lastName);
        firstName = view.findViewById(R.id.firstName);
        address = view.findViewById(R.id.address);
        phoneNumber = view.findViewById(R.id.phoneNumber);
        Button registerButton = view.findViewById(R.id.submitButton);

        // Register button click listener
        registerButton.setOnClickListener(v -> {
            if (validateForm()) {
                // Implement registration logic
                Toast.makeText(getContext(), "Registration successful", Toast.LENGTH_SHORT).show();
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
        if (TextUtils.isEmpty(lastName.getText())) {
            lastName.setError("Last Name is required");
            return false;
        }
        if (TextUtils.isEmpty(firstName.getText())) {
            firstName.setError("First name is required");
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

        return true;
    }

    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}";
        return email.matches(emailPattern);
    }
}