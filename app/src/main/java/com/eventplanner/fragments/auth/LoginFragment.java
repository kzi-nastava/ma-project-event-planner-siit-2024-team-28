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

public class LoginFragment extends Fragment {
    private EditText email, password;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_event_organizer_registration, container, false);

        // Initialize views
        email = view.findViewById(R.id.email);
        password = view.findViewById(R.id.password);
        Button loginButton = view.findViewById(R.id.submitButton);

        loginButton.setOnClickListener(v -> {
            if (validateForm()) {
                // Implement login logic
                Toast.makeText(getContext(), "Login successful", Toast.LENGTH_SHORT).show();
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

        return true;
    }

    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}";
        return email.matches(emailPattern);
    }
}