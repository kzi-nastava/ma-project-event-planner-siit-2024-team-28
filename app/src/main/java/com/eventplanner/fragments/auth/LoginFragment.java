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
import com.eventplanner.model.requests.LoginRequest;
import com.eventplanner.model.responses.AuthResponse;
import com.eventplanner.utils.ClientUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {
    private EditText email, password;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        email = view.findViewById(R.id.email);
        password = view.findViewById(R.id.password);
        Button loginButton = view.findViewById(R.id.submitButton);

        loginButton.setOnClickListener(v -> {
            if (validateForm()) {
                login();
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

    private void login() {
        LoginRequest request = new LoginRequest(
            email.getText().toString(),
            password.getText().toString()
        );

        new Thread(() -> {
            Call<AuthResponse> call = ClientUtils.authService.login(request);
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                    Toast.makeText(getContext(), "Login successful!", Toast.LENGTH_SHORT).show();
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