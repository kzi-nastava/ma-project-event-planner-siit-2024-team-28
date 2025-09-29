package com.eventplanner.fragments.solutions;

import android.graphics.Bitmap;
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

import com.bumptech.glide.Glide;
import com.eventplanner.R;
import com.eventplanner.databinding.FragmentSolutionHistoryDetailsBinding;
import com.eventplanner.model.enums.DurationType;
import com.eventplanner.model.responses.solutionHistories.GetSolutionHistoryDetails;
import com.eventplanner.services.EventService;
import com.eventplanner.services.SolutionHistoryService;
import com.eventplanner.services.UserService;
import com.eventplanner.utils.Base64Util;
import com.eventplanner.utils.HttpUtils;

import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SolutionHistoryDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SolutionHistoryDetailsFragment extends Fragment {
    private FragmentSolutionHistoryDetailsBinding binding;
    private String solutionId;
    private static final String ARG_SOLUTION_ID = "solutionId";
    private GetSolutionHistoryDetails solution;
    private SolutionHistoryService solutionHistoryService;
    private UserService userService;
    private EventService eventService;
    private NavController navController;
    private int globalImageIndex = 0;


    public SolutionHistoryDetailsFragment() {
        // Required empty public constructor
    }

    public static SolutionHistoryDetailsFragment newInstance(String solutionId) {
        SolutionHistoryDetailsFragment fragment = new SolutionHistoryDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SOLUTION_ID, solutionId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        solutionHistoryService = HttpUtils.getSolutionHistoryService();
        userService = HttpUtils.getUserService();
        eventService = HttpUtils.getEventService();
        navController = Navigation.findNavController(getActivity(), R.id.fragment_nav_content_main);
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            solutionId = getArguments().getString(ARG_SOLUTION_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSolutionHistoryDetailsBinding.inflate(inflater, container, false);
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
        Call<GetSolutionHistoryDetails> call = solutionHistoryService.getSolutionHistoryDetailsById(id);

        call.enqueue(new Callback<GetSolutionHistoryDetails>() {
            @Override
            public void onResponse(Call<GetSolutionHistoryDetails> call, Response<GetSolutionHistoryDetails> response) {
                if (response.isSuccessful()) {
                    solution = response.body();
                    if (solution != null) {
                        populateSolutionDetails();
                    }
                } else {
                    Log.e("SolutionHistoryDetailsFragment", "Error with fetching solution, response error code: " + response.code());
                    showErrorDialog();
                }
            }

            @Override
            public void onFailure(Call<GetSolutionHistoryDetails> call, Throwable t) {
                Log.e("SolutionHistoryDetailsFragment", "Network failure", t);
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
    // a bit messy
    private void populateSolutionDetails() {
        populateBasicInfo();
        populatePriceInfo();
        if (solution.getType().equals("Service")) {
            populateServiceSpecifics();
        } else {
            hideServiceSpecificViews();
        }

        // setting routing to business owner page
        binding.visitOwnerPageButton.setOnClickListener(v -> {
            Log.i("SolutionHistoryDetailsFragment", "Visiting business owner page for id: " + solution.getBusinessOwnerId());
            Bundle bundle = new Bundle();
            bundle.putString("businessOwnerId", String.valueOf(solution.getBusinessOwnerId()));
            navController.navigate(R.id.action_solutionHistoryDetails_to_businessOwnerDetails, bundle);
        });

        // setting routing to current solution details page
        binding.buttonSeeCurrentSolution.setOnClickListener(v -> {
            Log.i("SolutionHistoryDetailsFragment", "Visiting current solution details for id: " + solution.getSolutionId());
            Bundle bundle = new Bundle();
            bundle.putString("solutionId", String.valueOf(solution.getSolutionId()));
            navController.navigate(R.id.action_solutionHistoryDetails_to_solutionDetails, bundle);
        });
    }

    private void populateBasicInfo() {
        binding.textTitle.setText(solution.getName());
        binding.textCategory.setText(solution.getCategoryName());
        binding.textAuthor.setText(solution.getBusinessOwnerName());

        // Image
        if (solution.getImagesBase64() != null) {
            if (solution.getImagesBase64().isEmpty()) {
                // Glide is a library for efficient loading and displaying of images from various sources (URL, Base64, files, etc.),
                // which automatically caches images and optimizes memory usage. It helps load images quickly and smoothly without blocking the UI.
                Glide.with(requireContext())
                        .load(Base64Util.DEFAULT_IMAGE_URI)
                        .into(binding.imageSolution);
            } else {
                // Set first picture as current one
                Bitmap bitmap = Base64Util.decodeBase64ToBitmap(solution.getImagesBase64().get(globalImageIndex));
                binding.imageSolution.setImageBitmap(bitmap);
                // Listeners for buttons that cycle through pictures
                binding.buttonPreviousImage.setOnClickListener(this::changeImage);
                binding.buttonNextImage.setOnClickListener(this::changeImage);
            }
            // If there is no more than one picture hide buttons for cycling
            if (!(solution.getImagesBase64().size() > 1)) {
                binding.buttonPreviousImage.setVisibility(View.GONE);
                binding.buttonNextImage.setVisibility(View.GONE);
            }
        }

        String availabilityStatus = (solution.getIsAvailable()) ? "Available" : "Unavailable";
        binding.textAvailability.setText(binding.textAvailability.getText() + ": " + availabilityStatus);
        binding.textDescription.setText(binding.textDescription.getText() + ": " + solution.getDescription());
        binding.textEventTypes.setText(binding.textEventTypes.getText() + ": " + solution.getEventTypeNames().stream()
                .map(String::valueOf).collect(Collectors.joining(", ")));
    }

    private void populatePriceInfo() {
        binding.textPrice.setText(String.format("Price: %.2f$", solution.getPrice()));
        if (solution.getDiscount() > 0) {
            binding.textDiscount.setText(String.format("Discounted Price: %.2f$", calculateFinalPrice(solution.getPrice(), solution.getDiscount())));
            binding.textPrice.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray));
            binding.textDiscount.setVisibility(View.VISIBLE);
        } else {
            binding.textDiscount.setVisibility(View.GONE);
        }
    }

    private void populateServiceSpecifics() {
        binding.textSpecifics.setText("Specifics: " + solution.getSpecifics());
        if (solution.getDurationType() == DurationType.FIXED) {
            binding.textMinDuration.setVisibility(View.GONE);
            binding.textMaxDuration.setVisibility(View.GONE);
            binding.textDuration.setText(String.format("Duration: %.2f hrs", convertSecondsToHours(solution.getFixedDurationInSeconds())));
        } else {
            binding.textDuration.setVisibility(View.GONE);
            binding.textMinDuration.setText(String.format("Min Duration: %.2f hrs", convertSecondsToHours(solution.getMinDurationInSeconds())));
            binding.textMaxDuration.setText(String.format("Max Duration: %.2f hrs", convertSecondsToHours(solution.getMaxDurationInSeconds())));
        }
        binding.textReservationDeadline.setText("Reservation Deadline: " + solution.getReservationDeadlineDays() + " days beforehand");
        binding.textCancellationDeadline.setText("Cancellation Deadline: " + solution.getCancellationDeadlineDays() + " days beforehand");
        binding.textReservationType.setText("Reservation Type: " + solution.getReservationType());
    }

    private void hideServiceSpecificViews() {
        binding.textSpecifics.setVisibility(View.GONE);
        binding.textDuration.setVisibility(View.GONE);
        binding.textMinDuration.setVisibility(View.GONE);
        binding.textMaxDuration.setVisibility(View.GONE);
        binding.textReservationDeadline.setVisibility(View.GONE);
        binding.textCancellationDeadline.setVisibility(View.GONE);
        binding.textReservationType.setVisibility(View.GONE);
    }

    // Changes image in image view
    // Operates with globalImageIndex -> ++ if buttonNextImage; -- if buttonPreviousImage
    private void changeImage(View v) {
        int lastIndex = solution.getImagesBase64().size() - 1;
        // If buttonPreviousImage is clicked
        if (v == binding.buttonPreviousImage) {
            // If current image is the first one and previous is clicked -> set last image as current one
            if (globalImageIndex == 0)
                globalImageIndex = lastIndex;
            else
                globalImageIndex--;
        }
        // If buttonNextImage is clicked
        if (v == binding.buttonNextImage) {
            // If current image is the last one and next is clicked -> set first image as current one
            if (globalImageIndex == lastIndex)
                globalImageIndex = 0;
            else
                globalImageIndex++;
        }
        Bitmap bitmap = Base64Util.decodeBase64ToBitmap(solution.getImagesBase64().get(globalImageIndex));
        binding.imageSolution.setImageBitmap(bitmap);
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