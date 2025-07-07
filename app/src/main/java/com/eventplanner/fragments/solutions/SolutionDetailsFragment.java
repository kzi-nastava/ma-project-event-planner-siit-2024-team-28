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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.eventplanner.R;
import com.eventplanner.databinding.FragmentSolutionDetailsBinding;
import com.eventplanner.model.responses.eventTypes.GetEventTypeResponse;
import com.eventplanner.model.responses.events.GetEventResponse;
import com.eventplanner.model.responses.solutionCateogries.GetSolutionCategoryResponse;
import com.eventplanner.model.responses.solutions.GetSolutionDetailsResponse;
import com.eventplanner.model.responses.solutions.GetSolutionResponse;
import com.eventplanner.model.responses.users.GetUserResponse;
import com.eventplanner.services.EventService;
import com.eventplanner.services.EventTypeService;
import com.eventplanner.services.ProductService;
import com.eventplanner.services.SolutionCategoryService;
import com.eventplanner.services.SolutionService;
import com.eventplanner.services.UserService;
import com.eventplanner.utils.AuthUtils;
import com.eventplanner.utils.HttpUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SolutionDetailsFragment extends Fragment {
    private FragmentSolutionDetailsBinding binding;
    private String solutionId;
    private static final String ARG_SOLUTION_ID = "solutionId";
    private GetSolutionDetailsResponse solution;
    private SolutionService solutionService;
    private UserService userService;
    private EventService eventService;
    private ProductService productService;


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
        userService = HttpUtils.getUserService();
        eventService = HttpUtils.getEventService();
        productService = HttpUtils.getProductService();
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            solutionId = getArguments().getString(ARG_SOLUTION_ID);
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

    private void addToFavorites(Long userId, Long serviceId) {
        Call<String> call = userService.favoriteService(userId, serviceId);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    String message = response.body();
                    Log.d("SolutionDetailsFragment", "Successfully added to favorites: " + message);
                    // show message from backend
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("SolutionDetailsFragment", "Error: " + response.code());
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

    /**
     *  Since buying logic depends on bunch of asynchronous responses:
     *                                                        - response from backend about active events
     *                                                        - response from user about selected event for which solution is buying
     *  'buy logic' is organized as a series of asynchronous event-driven callbacks (events -> response from backend & response from user)
     */
    private void buy() {
        // Even though "buy button" is hidden from every other user we check just in case
        if(!AuthUtils.getUserRoles(getContext()).contains("EventOrganizer"))
            return;

        // 'Solution' can be either 'Service' or 'Product' -> two different buying business logics
        if (solution.getType().equals("Product")) {
            // Fetching active events from backend and making a dialog for user
            fetchActiveEvents(
                    events -> showEventSelectionDialog(events, this::buyProduct) // buyProduct() as callback method
            );
        } else if (solution.getType().equals("Service")) {
            // Fetching active events from backend and making a dialog for user
            fetchActiveEvents(
                    events -> showEventSelectionDialog(events, this::buyService) // buyService() as callback method
            );
        }

    }

    /**
     * Fetches active events asynchronously from backend.
     * Calls onSuccess with the list of events on success.
     */
    private void fetchActiveEvents(Consumer<List<GetEventResponse>> onSuccess) {
        Long eventOrganizerId = AuthUtils.getUserId(getContext());
        Call<Collection<GetEventResponse>> call = eventService.getActiveEventsByOrganizer(eventOrganizerId);

        call.enqueue(new Callback<Collection<GetEventResponse>>() {
            @Override
            public void onResponse(Call<Collection<GetEventResponse>> call, Response<Collection<GetEventResponse>> response) {
                if (response.isSuccessful()) {
                    List<GetEventResponse> events = new ArrayList<>(response.body());
                    Log.d("SolutionDetailsFragment", "Fetched events: " + events.size() + " events");
                    onSuccess.accept(events); // callback method call
                } else {
                    Log.e("SolutionDetailsFragment", "Failed fetching events: " + response.code());
                    Toast.makeText(getContext(), "Failed to load events", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Collection<GetEventResponse>> call, Throwable t) {
                Log.e("SolutionDetailsFragment", "Network error", t);
                Toast.makeText(getContext(), "Network failure", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Shows dialog for user to select an event.
     * Calls onResponse with the selected event ID.
     */
    private void showEventSelectionDialog(List<GetEventResponse> events, Consumer<Long> onResponse) {
        if (events == null || events.isEmpty()) {
            new AlertDialog.Builder(getContext())
                    .setTitle("No Events Available")
                    .setMessage("Currently, there are no active events to choose from.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        String[] eventNames = new String[events.size()];
        for (int i = 0; i < events.size(); i++) {
            eventNames[i] = events.get(i).getName();
        }

        final int[] selectedIndex = {0}; // first one is selected by default just because

        new AlertDialog.Builder(getContext())
                .setTitle("Select Event")
                .setSingleChoiceItems(eventNames, 0, (dialog, which) -> selectedIndex[0] = which)
                .setPositiveButton("OK", (dialog, which) -> {
                    Long selectedEventId = events.get(selectedIndex[0]).getId();
                    onResponse.accept(selectedEventId);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Function for making call to backend for buying a product
     */
    private void buyProduct(Long selectedEventId) {
        Long productId = solution.getId();

        productService.buyProduct(productId, selectedEventId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Product purchased successfully!", Toast.LENGTH_SHORT).show();
                    Log.i("SolutionDetailsFragment", "Buying product went successfully.");
                } else {
                    Toast.makeText(getContext(), "Failed to purchase product: " + response.code(), Toast.LENGTH_SHORT).show();
                    Log.i("SolutionDetailsFragment", "Failed to purchase product: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Network error while purchasing product", Toast.LENGTH_SHORT).show();
                Log.i("SolutionDetailsFragment", "Network failure: " + t.getMessage());
            }
        });
    }

    /**
     * Function for making call to backend for buying a service
     */
    private void buyService(Long selectedEventId) {
        // TODO: make call to backend
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
            Log.i("SolutionDetailsFragment", "Visiting business owner page for id: " + solution.getBusinessOwnerId());
            Bundle bundle = new Bundle();
            bundle.putString("businessOwnerId", String.valueOf(solution.getBusinessOwnerId()));
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_soltuionDetails_to_businessOwnerDetails, bundle);
        });

        binding.buttonBuy.setOnClickListener( v -> {
            buy();
        });

        // button for adding to favorites should be only visible to EventOrganizers
        List<String> roles = AuthUtils.getUserRoles(getContext());
        if(roles.contains("EventOrganizer")) {
            binding.addToFavorites.setOnClickListener(v -> {
                Long userId = AuthUtils.getUserId(getContext());
                addToFavorites(userId, Long.parseLong(solutionId));
            });
        } else
            binding.addToFavorites.setVisibility(View.GONE);
    }

    private void populateBasicInfo() {
        binding.textTitle.setText(solution.getName());
        binding.textCategory.setText(solution.getCategoryName());
        binding.textAuthor.setText(solution.getBusinessOwnerName());
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
        if (solution.getFixedDurationInSeconds() != null) {
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