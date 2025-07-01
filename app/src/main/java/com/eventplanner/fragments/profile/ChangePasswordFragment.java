package com.eventplanner.fragments.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.eventplanner.R;
import com.eventplanner.model.requests.auth.UpdatePasswordRequest;
import com.eventplanner.utils.AuthUtils;
import com.eventplanner.utils.FormValidator;
import com.eventplanner.utils.HttpUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordFragment extends Fragment {
    private EditText oldPassword, newPassword, confirmNewPassword;
    private Long userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_password, container, false);

        oldPassword = view.findViewById(R.id.oldPassword);
        newPassword = view.findViewById(R.id.newPassword);
        confirmNewPassword = view.findViewById(R.id.confirmNewPassword);

        userId = AuthUtils.getUserId(requireContext());

        Button submitButton = view.findViewById(R.id.submitButton);
        submitButton.setOnClickListener(v -> changePassword());
        submitButton.setText(getString(R.string.change_password_button));

        Button cancelButton = view.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(v -> requireActivity().onBackPressed());
        cancelButton.setText(getString(R.string.cancel_button));

        // Set hints from strings.xml
        oldPassword.setHint(getString(R.string.hint_current_password));
        newPassword.setHint(getString(R.string.hint_new_password));
        confirmNewPassword.setHint(getString(R.string.hint_confirm_new_password));

        return view;
    }

    private boolean validateForm() {
        boolean valid = true;

        String oldPass = oldPassword.getText().toString();
        if (FormValidator.isEmpty(oldPass)) {
            oldPassword.setError(getString(R.string.error_current_password_required));
            valid = false;
        }

        String newPass = newPassword.getText().toString();
        if (FormValidator.isEmpty(newPass)) {
            newPassword.setError(getString(R.string.error_new_password_required));
            valid = false;
        } else if (!FormValidator.isValidPassword(newPass)) {
            newPassword.setError(getString(R.string.error_password_invalid));
            valid = false;
        }

        String confirmPass = confirmNewPassword.getText().toString();
        if (FormValidator.isEmpty(confirmPass)) {
            confirmNewPassword.setError(getString(R.string.error_confirm_password_required));
            valid = false;
        } else if (!confirmPass.equals(newPass)) {
            confirmNewPassword.setError(getString(R.string.error_passwords_mismatch));
            valid = false;
        }

        return valid;
    }

    private void changePassword() {
        if (!validateForm() || userId == null) return;

        UpdatePasswordRequest request = new UpdatePasswordRequest(
                oldPassword.getText().toString(),
                newPassword.getText().toString()
        );

        Call<Void> call = HttpUtils.getUserService().updateUserPassword(userId, request);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), getString(R.string.toast_password_changed_success), Toast.LENGTH_SHORT).show();
                    requireActivity().onBackPressed();
                } else {
                    Toast.makeText(getContext(), getString(R.string.toast_password_change_failed), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), getString(R.string.toast_network_error), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
