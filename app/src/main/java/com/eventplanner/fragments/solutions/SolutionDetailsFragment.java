package com.eventplanner.fragments.solutions;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.eventplanner.R;
import com.eventplanner.databinding.FragmentSolutionDetailsBinding;
import com.eventplanner.model.responses.eventTypes.GetEventTypeResponse;
import com.eventplanner.model.responses.solutionCateogries.GetSolutionCategoryResponse;
import com.eventplanner.model.responses.solutions.GetSolutionDetailsResponse;
import com.eventplanner.model.responses.solutions.GetSolutionResponse;
import com.eventplanner.model.responses.users.GetUserResponse;
import com.eventplanner.services.EventTypeService;
import com.eventplanner.services.SolutionCategoryService;
import com.eventplanner.services.SolutionService;
import com.eventplanner.services.UserService;
import com.eventplanner.utils.HttpUtils;

import java.util.ArrayList;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SolutionDetailsFragment extends Fragment {
    private FragmentSolutionDetailsBinding binding;
    private SolutionService solutionService;
    private static final String ARG_SOLUTION_ID = "solutionId";
    private String solutionId;
    private GetSolutionDetailsResponse solution;


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
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            solutionId = getArguments().getString(ARG_SOLUTION_ID);
            solutionId = "7"; // TODO: skloniti kad se sredi prosledjivanje id-eva
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
        fetchSolutionDetails();
    }

    private void fetchSolutionDetails() {
        Long id = Long.parseLong(solutionId);
        Call<GetSolutionDetailsResponse> call = solutionService.getSolutionDetailsById(id);

        call.enqueue(new Callback<GetSolutionDetailsResponse>() {
            @Override
            public void onResponse(Call<GetSolutionDetailsResponse> call, Response<GetSolutionDetailsResponse> response) {
                if (response.isSuccessful()) {
                    solution = response.body();
                    if (solution != null) {
                        populateSolutionDetails();
                    }
                } else {
                    Log.e("SolutionDetailsFragment", "Error with fetching solution, response error code: " + response.code());
                    showErrorDialog();
                }
            }

            @Override
            public void onFailure(Call<GetSolutionDetailsResponse> call, Throwable t) {
                Log.e("SolutionDetailsFragment", "Network failure", t);
                showErrorDialog();
            }
        });
    }

    private void showErrorDialog() {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setMessage("An error has occured.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }


    // method used to separate code for altering views from main code
    private void populateSolutionDetails() {
        binding.textTitle.setText(solution.getName());
        binding.textCategory.setText(solution.getCategoryName());
        binding.textAuthor.setText(solution.getBusinessOwnerName());
        binding.textPrice.setText(binding.textPrice.getText() + ": " + String.format("%.2f", solution.getPrice()) + "$");

        // if discount exists show actual price and set gray color to original price
        if(solution.getDiscount() > 0) {
            binding.textDiscount.setText(binding.textDiscount.getText() + ": " + String.format("%.2f", calculateFinalPrice(solution.getPrice(), solution.getDiscount())) + "$");
            binding.textPrice.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray));
        }
        else {
            binding.textDiscount.setVisibility(View.GONE);
        }

        String availabilityStatus = (solution.getIsAvailable()) ? "Available" : "Unavailable";
        binding.textAvailability.setText(binding.textAvailability.getText() + ": " + availabilityStatus);
        binding.textDescription.setText(binding.textDescription.getText() + ": " + solution.getDescription());
        binding.textEventTypes.setText(binding.textEventTypes.getText() + ": " + solution.getEventTypeNames().stream()
                .map(String::valueOf).collect(Collectors.joining(", ")));
        // fields only service has
        if(!solution.getType().equals("Service")) {
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

        binding.visitOwnerPageButton.setOnClickListener(v -> {
            Log.i("SolutionDetailsFragment", "Visiting business owner page for id: " + solution.getBusinessOwnerId());
            Bundle bundle = new Bundle();
            bundle.putString("businessOwnerId", String.valueOf(solution.getBusinessOwnerId()));
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_soltuionDetails_to_businessOwnerDetails, bundle);
        });
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