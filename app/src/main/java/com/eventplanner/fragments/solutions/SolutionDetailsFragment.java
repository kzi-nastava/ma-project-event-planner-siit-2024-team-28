package com.eventplanner.fragments.solutions;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.eventplanner.R;
import com.eventplanner.databinding.FragmentSolutionDetailsBinding;
import com.eventplanner.model.responses.solutionCateogries.GetSolutionCategoryResponse;
import com.eventplanner.model.responses.solutions.GetSolutionResponse;
import com.eventplanner.model.responses.users.GetUserResponse;
import com.eventplanner.services.SolutionCategoryService;
import com.eventplanner.services.SolutionService;
import com.eventplanner.services.UserService;
import com.eventplanner.utils.HttpUtils;

import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SolutionDetailsFragment extends Fragment {
    private FragmentSolutionDetailsBinding binding;
    private SolutionService solutionService;

    private SolutionCategoryService solutionCategoryService;
    private UserService userService;
    private static final String ARG_SOLUTION_ID = "solutionId";
    private String solutionId;
    private GetSolutionResponse solution;
    private String solutionType;

    private GetSolutionCategoryResponse solutionCategory;
    private GetUserResponse businessOwner;

    // fields used to sync two calls to backhand
    private boolean isSolutionDetailsLoaded = false;
    private boolean isSolutionTypeLoaded = false;

    public static SolutionDetailsFragment newInstance(String solutionId) {
        SolutionDetailsFragment fragment = new SolutionDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SOLUTION_ID, solutionId);
        fragment.setArguments(args);
        return fragment;
    }

    public SolutionDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        solutionService = HttpUtils.getSolutionService();
        solutionCategoryService = HttpUtils.getSolutionCategoryService();
        userService = HttpUtils.getUserService();
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            solutionId = getArguments().getString(ARG_SOLUTION_ID);
            solutionId = "14"; // TODO: skloniti kad se sredi prosledjivanje id-eva
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate layout
        binding = FragmentSolutionDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // getting solution details from backend
        fetchSolutionDetails(view);
        fetchSolutionType(view);
    }

    private void fetchSolutionDetails(View view) {
        Long id = Long.parseLong(solutionId);
        Call<GetSolutionResponse> call = solutionService.getSolutionById(id);

        call.enqueue(new Callback<GetSolutionResponse>() {
            @Override
            public void onResponse(Call<GetSolutionResponse> call, Response<GetSolutionResponse> response) {
                if (response.isSuccessful()) {
                    solution = response.body();
                    if (solution != null) {
                        isSolutionDetailsLoaded = true;
                        checkAndPopulate();
                        fetchSolutionCategory(solution.getCategoryId());
                        fetchBusinessOwner(solution.getBusinessOwnerId());
                    }
                } else {
                    Log.e("SolutionDetailsFragment", "Error with fetching solution, response error code: " + response.code());
                    showErrorDialog();
                }
            }

            @Override
            public void onFailure(Call<GetSolutionResponse> call, Throwable t) {
                Log.e("SolutionDetailsFragment", "Network failure", t);
                showErrorDialog();
            }
        });
    }

    private void fetchSolutionType(View view) {
        Long id = Long.parseLong(solutionId);
        Call<String> call = solutionService.getSolutionType(id);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    isSolutionTypeLoaded = true;
                    solutionType = response.body();
                    checkAndPopulate();
                } else {
                    Log.e("SolutionDetailsFragment", "Error with fetching solution type, response error code: " + response.code());
                    showErrorDialog();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("SolutionDetailsFragment", "Network failure", t);
                showErrorDialog();
            }
        });
    }

    private void fetchSolutionCategory(Long categoryId) {
        Call<GetSolutionCategoryResponse> call = solutionCategoryService.getSolutionCategoryById(categoryId);

        call.enqueue(new Callback<GetSolutionCategoryResponse>() {
            @Override
            public void onResponse(Call<GetSolutionCategoryResponse> call, Response<GetSolutionCategoryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    solutionCategory = response.body();
                    binding.textCategory.setText(solutionCategory.getName());
                } else {
                    Log.e("SolutionDetailsFragment", "Failed to load category: " + response.code());
                    showErrorDialog();
                }
            }

            @Override
            public void onFailure(Call<GetSolutionCategoryResponse> call, Throwable t) {
                Log.e("SolutionDetailsFragment", "Network error while fetching category", t);
                showErrorDialog();
            }
        });
    }

    private void fetchBusinessOwner(Long userId) {
        Call<GetUserResponse> call = userService.getUserById(userId);

        call.enqueue(new Callback<GetUserResponse>() {
            @Override
            public void onResponse(Call<GetUserResponse> call, Response<GetUserResponse> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        businessOwner = response.body();
                        binding.textAuthor.setText(businessOwner.getFirstName() + " " + businessOwner.getLastName());
                    }
                } else {
                    Log.e("User", "Error while fetching user: " + response.code());
                    showErrorDialog();
                }
            }

            @Override
            public void onFailure(Call<GetUserResponse> call, Throwable t) {
                Log.e("User", "Network error while fetching user", t);
                showErrorDialog();
            }
        });
    }

    private void showErrorDialog() {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setMessage("There has been an error.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }

    private void checkAndPopulate() {
        if(isSolutionDetailsLoaded && isSolutionTypeLoaded)
            populateSolutionDetails();
    }

    // method used to separate code for altering views from main code
    private void populateSolutionDetails() {
        binding.textTitle.setText(solution.getName());
        // category name is set in fetchSolutionCategory method
        // businessOwner name is set in fetchBusinessOwner method
        binding.textPrice.setText(binding.textPrice.getText() + ": " + String.format("%.2f", solution.getPrice()) + "$");

        if(solution.getDiscount() > 0) {
            binding.textDiscount.setText(binding.textDiscount.getText() + ": " + String.format("%.2f", calculateFinalPrice(solution.getPrice(), solution.getDiscount())) + "$");
        }
        else {
            binding.textDiscount.setVisibility(View.GONE);
        }

        String availabilityStatus = (solution.getIsAvailable()) ? "Available" : "Unavailable";
        binding.textAvailability.setText(binding.textAvailability.getText() + ": " + availabilityStatus);
        binding.textDescription.setText(binding.textDescription.getText() + ": " + solution.getDescription());
        binding.textEventTypes.setText(binding.textEventTypes.getText() + ": " + solution.getEventTypeIds().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "))); // TODO: srediti

        // fields only service has
        if(!solutionType.equals("Service")) {
            // remove excessive views | views no other solution has except service
            binding.textSpecifics.setVisibility(View.GONE);
            binding.textDuration.setVisibility(View.GONE);
            binding.textMinDuration.setVisibility(View.GONE);
            binding.textMaxDuration.setVisibility(View.GONE);
            binding.textReservationDeadline.setVisibility(View.GONE);
            binding.textCancellationDeadline.setVisibility(View.GONE);
            binding.textReservationType.setVisibility(View.GONE);
        }
        else {
            binding.textSpecifics.setText(binding.textSpecifics.getText() + ": " + solution.getSpecifics());

            // solution (service) can either have fixed duration or minmax duration if fixed duration is non existent solution has minmax durations and vice versa
            if (solution.getFixedDurationInSeconds() != null) {
                // remove excessive views
                binding.textMinDuration.setVisibility(View.GONE);
                binding.textMaxDuration.setVisibility(View.GONE);
                binding.textDuration.setText(binding.textDuration.getText() + ": " + String.format("%.2f", convertSecondsToHours(solution.getFixedDurationInSeconds())) + " hrs");
            } else {
                // hide excessive view
                binding.textDuration.setVisibility(View.GONE);

                binding.textMinDuration.setText(binding.textMinDuration.getText() + ": " + String.format("%.2f", convertSecondsToHours(solution.getMinDurationInSeconds())) + " hrs");
                binding.textMaxDuration.setText(binding.textMaxDuration.getText() + ": " + String.format("%.2f", convertSecondsToHours(solution.getMaxDurationInSeconds())) + " hrs");
            }

            binding.textReservationDeadline.setText(binding.textReservationDeadline.getText() + ": " + solution.getReservationDeadlineDays().toString() + " days beforehand");
            binding.textCancellationDeadline.setText(binding.textCancellationDeadline.getText() + ": " + solution.getCancellationDeadlineDays().toString() + " days beforehand");
            binding.textReservationType.setText(binding.textReservationType.getText() + ": " + solution.getReservationType().toString());
        }
    }

    private Double convertSecondsToHours(Integer seconds) {
        Integer hours = seconds / 3600;
        Integer minutes = (seconds % 3600) / 60;

        Double decimalHours = hours + (minutes / 60.0);
        return decimalHours;
    }

    private Double calculateFinalPrice(Double originalPrice, Double discountPercent) {
        Double discountAmount = originalPrice * (discountPercent / 100.0);
        return originalPrice - discountAmount;
    }
}