package com.eventplanner.fragments.users;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eventplanner.R;
import com.eventplanner.databinding.FragmentBusinessOwnerDetailsBinding;
import com.eventplanner.databinding.FragmentSolutionDetailsBinding;
import com.eventplanner.model.responses.users.GetUserResponse;
import com.eventplanner.services.UserService;
import com.eventplanner.utils.HttpUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class BusinessOwnerDetailsFragment extends Fragment {

    private FragmentBusinessOwnerDetailsBinding binding;
    private static final String ARG_BUSINESS_OWNER_ID = "businessOwnerId";
    private UserService userService;
    private String businessOwnerId;
    private GetUserResponse businessOwner;

    public BusinessOwnerDetailsFragment() {
        // Required empty public constructor
    }

    public static BusinessOwnerDetailsFragment newInstance(String businessOwnerId) {
        BusinessOwnerDetailsFragment fragment = new BusinessOwnerDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_BUSINESS_OWNER_ID, businessOwnerId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userService = HttpUtils.getUserService();
        if (getArguments() != null) {
            businessOwnerId = getArguments().getString(ARG_BUSINESS_OWNER_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentBusinessOwnerDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // getting business owner details from backend
        fetchBusinessOwnerDetails();
    }

    private void fetchBusinessOwnerDetails() {
        Call<GetUserResponse> call = userService.getUserById(Long.parseLong(businessOwnerId));
        call.enqueue(new Callback<GetUserResponse>() {
            @Override
            public void onResponse(Call<GetUserResponse> call, Response<GetUserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    businessOwner = response.body();
                    populateViews();
                    Log.i("BusinessOwnerDetailsFragment", "Business owner successfully fetched with id: " + businessOwnerId);
                } else {
                    Log.w("BusinessOwnerDetailsFragment", "Error with fetching business owner: " + response.code());
                    showErrorDialog();

                }
            }

            @Override
            public void onFailure(Call<GetUserResponse> call, Throwable t) {
                Log.e("BusinessOwnerDetailsFragment", "Network failure", t);
                showErrorDialog();
            }
        });
    }

    private void populateViews() {
        binding.textBusinessName.setText(businessOwner.getBusinessName());
        binding.textAddress.setText(binding.textAddress.getText() + " " + businessOwner.getAddress());
        binding.textDescription.setText(binding.textDescription.getText() + " " + businessOwner.getBusinessDescription());
        binding.textPhoneNumber.setText(binding.textPhoneNumber.getText() + " " + businessOwner.getPhoneNumber());
        binding.reportUserButton.setOnClickListener(v -> {
            Log.i("BusinessOwnerFragment", "Attempt creating report.");
            createReport();
        });
    }

    private void createReport() {

    }

    private void showErrorDialog() {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setMessage("An error has occured.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }


}