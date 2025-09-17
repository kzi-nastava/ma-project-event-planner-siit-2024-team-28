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
import com.eventplanner.activities.HomeActivity;
import com.eventplanner.model.requests.auth.LoginRequest;
import com.eventplanner.model.responses.auth.AuthResponse;
import com.eventplanner.utils.AuthUtils;
import com.eventplanner.utils.FormValidator;
import com.eventplanner.utils.HttpUtils;

import org.json.JSONObject;

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
        boolean valid = true;

        String emailInput = email.getText().toString().trim();
        String passwordInput = password.getText().toString();

        if (!FormValidator.isValidEmail(emailInput)) {
            email.setError(getString(R.string.error_email_invalid));
            valid = false;
        } else {
            email.setError(null);
        }

        if (FormValidator.isEmpty(passwordInput)) {
            password.setError(getString(R.string.error_password_required));
            valid = false;
        } else {
            password.setError(null);
        }

        return valid;
    }

    private void login() {
        LoginRequest request = new LoginRequest(
                email.getText().toString(),
                password.getText().toString()
        );

        new Thread(() -> {
            Call<AuthResponse> call = HttpUtils.getAuthService().login(request);
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getJwtToken() != null && getActivity() != null && getActivity().getApplicationContext() != null) {
                        AuthUtils.saveToken(getActivity().getApplicationContext(), response.body().getJwtToken());

                        // Update navigation menu after login
                        if (getActivity() instanceof HomeActivity) {
                            ((HomeActivity) getActivity()).updateNavMenu();
                        }

                        // Navigate back to HomeFragment
                        View view = getView();
                        if (view != null) {
                            Navigation.findNavController(view).navigate(R.id.nav_home);
                        }
                    } else {
                        // Try to extract error message from response
                        String errorMessage = getString(R.string.toast_invalid_credentials); // fallback
                        try {
                            if (response.errorBody() != null) {
                                String errorBody = response.errorBody().string();
                                // Parse JSON: {"error":"Invalid credentials"}
                                JSONObject json = new JSONObject(errorBody);
                                if (json.has("error")) {
                                    errorMessage = json.getString("error");
                                    if(errorMessage.equals("Bad credentials"))
                                    {
                                        errorMessage = getString(R.string.toast_invalid_credentials);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                    Toast.makeText(getContext(), getString(R.string.toast_login_failed), Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
}