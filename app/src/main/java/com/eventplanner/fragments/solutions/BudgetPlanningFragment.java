package com.eventplanner.fragments.solutions;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.eventplanner.R;
import com.eventplanner.databinding.FragmentBudgetPlanningBinding;
import com.eventplanner.model.requests.requiredSolutions.CreateRequiredSolutionRequest;
import com.eventplanner.model.responses.ErrorResponse;
import com.eventplanner.model.responses.events.GetEventResponse;
import com.eventplanner.model.responses.solutionCateogries.GetSolutionCategoryResponse;
import com.eventplanner.services.EventService;
import com.eventplanner.services.EventTypeService;
import com.eventplanner.services.RequiredSolutionService;
import com.eventplanner.utils.AuthUtils;
import com.eventplanner.utils.HttpUtils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BudgetPlanningFragment extends Fragment {
    FragmentBudgetPlanningBinding binding;
    EventService eventService;
    RequiredSolutionService requiredSolutionService;
    EventTypeService eventTypeService;
    NavController navController;
    Long eventOrganizerId;
    Long selectedEventId;
    Long selectedCategoryId;

    public BudgetPlanningFragment() {
        // Required empty public constructor
    }

    public static BudgetPlanningFragment newInstance(String param1, String param2) {
        BudgetPlanningFragment fragment = new BudgetPlanningFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventService = HttpUtils.getEventService();
        requiredSolutionService = HttpUtils.getRequiredSolutionService();
        eventTypeService = HttpUtils.getEventTypeService();
        eventOrganizerId = AuthUtils.getUserId(getContext());
        navController = Navigation.findNavController(getActivity(), R.id.fragment_nav_content_main);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBudgetPlanningBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        setupViews();

        return view;
    }

    private void setupViews() {
        fetchActiveEvents();
        binding.buttonCreateItem.setOnClickListener(v -> {
            createItem();
        });
        binding.buttonShowItems.setOnClickListener(v -> {
            if(selectedEventId == null) {
                Toast.makeText(getContext(), "Select an event first.", Toast.LENGTH_SHORT).show();
                return;
            }
            Bundle args = new Bundle();
            args.putLong("eventId", selectedEventId);
            navController.navigate(R.id.action_budget_planning_to_budget_planning_items, args);
        });
    }

    private void fetchActiveEvents() {
        Call<Collection<GetEventResponse>> call = eventService.getActiveEventsByOrganizer(eventOrganizerId);

        call.enqueue(new Callback<Collection<GetEventResponse>>() {
            @Override
            public void onResponse(Call<Collection<GetEventResponse>> call, Response<Collection<GetEventResponse>> response) {
                if (response.isSuccessful()) {
                    List<GetEventResponse> events = new ArrayList<>(response.body());

                    List<String> eventNames = new ArrayList<>();
                    eventNames.add("Select event"); // placeholder
                    for (GetEventResponse event : events) {
                        eventNames.add(event.getName());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            eventNames
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    Spinner spinner = binding.spinnerEvents;
                    spinner.setAdapter(adapter);

                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (position == 0) {
                                selectedEventId = null;
                            } else {
                                selectedEventId = events.get(position - 1).getId();
                                fetchEvent();
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });
                    Log.d("BudgetPlanningFragment", "Fetched events: " + events.size() + " events");
                } else {
                    Log.e("BudgetPlanningFragment", "Failed fetching events: " + response.code());
                    Toast.makeText(getContext(), "Failed to load events", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Collection<GetEventResponse>> call, Throwable t) {
                Log.e("BudgetPlanningFragment", "Network error", t);
                Toast.makeText(getContext(), "Network failure", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchEvent() {
        Call<GetEventResponse> call = eventService.getEventById(selectedEventId);
        call.enqueue(new Callback<GetEventResponse>() {
            @Override
            public void onResponse(Call<GetEventResponse> call, Response<GetEventResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GetEventResponse event = response.body();

                    fetchRecommendedCategories(event.getEventTypeId());

                    Log.d("BudgetPlanningFragment", "Event successfully fetched: " + event.getName());
                } else {
                    Log.e("BudgetPlanningFragment", "Failed to load event: " + response.code());
                    Toast.makeText(getContext(), "Failed to load event", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GetEventResponse> call, Throwable t) {
                Log.e("BudgetPlanningFragment", "Network failure", t);
                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchRecommendedCategories(Long eventTypeId) {
        Call<Collection<GetSolutionCategoryResponse>> call = eventTypeService.getRecommendedCategories(eventTypeId);
        call.enqueue(new Callback<Collection<GetSolutionCategoryResponse>>() {
            @Override
            public void onResponse(Call<Collection<GetSolutionCategoryResponse>> call, Response<Collection<GetSolutionCategoryResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<GetSolutionCategoryResponse> categories = new ArrayList<>(response.body());

                    List<String> categoryNames = new ArrayList<>();
                    categoryNames.add("Select category"); // placeholder
                    for (GetSolutionCategoryResponse category : categories) {
                        categoryNames.add(category.getName());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            categoryNames
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    Spinner spinner = binding.spinnerCategories;
                    spinner.setAdapter(adapter);

                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (position == 0) {
                                selectedCategoryId = null;
                            } else {
                                selectedCategoryId = categories.get(position - 1).getId();
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });
                    Log.d("BudgetPlanningFragment", "Recommended categories successfully fetched: " + categories.size());
                } else {
                    Log.e("BudgetPlanningFragment", "Failed to load recommended categories: " + response.code());
                    Toast.makeText(getContext(), "Failed to load recommended categories", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Collection<GetSolutionCategoryResponse>> call, Throwable t) {
                Log.e("BudgetPlanningFragment", "Network error while loading recommended categories", t);
                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createItem() {
        // Validation
        if (selectedEventId == null) {
            Toast.makeText(getContext(), "You have to select an event.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedCategoryId == null) {
            Toast.makeText(getContext(), "You have to select a category.", Toast.LENGTH_SHORT).show();
            return;
        }
        String amountText = binding.editTextAmount.getText().toString().trim();
        Double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (Exception e) {
            if (amountText.isEmpty()) {
                Toast.makeText(getContext(), "You have to enter amount.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Invalid input for amount.", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        CreateRequiredSolutionRequest request = new CreateRequiredSolutionRequest.Builder()
                .budget(amount)
                .solutionId(null)
                .categoryId(selectedCategoryId)
                .eventId(selectedEventId)
                .build();

        Call<Long> call = requiredSolutionService.createRequiredSolution(request);
        call.enqueue(new Callback<Long>() {
            @Override
            public void onResponse(Call<Long> call, Response<Long> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Long newId = response.body();
                    Log.d("BudgetPlanningFragment", "Created RequiredSolution with id: " + newId);
                    Toast.makeText(getContext(), "Required solution created successfully.", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        String errorJson = response.errorBody().string();
                        Gson gson = new Gson();
                        ErrorResponse errorResponse = gson.fromJson(errorJson, ErrorResponse.class);
                        Toast.makeText(getContext(), errorResponse.getError(), Toast.LENGTH_SHORT).show();
                        Log.i("BudgetPlanningFragment", "Failed to create required solution: " + errorResponse.getError());
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Failed to create required solution: unknown error", Toast.LENGTH_SHORT).show();
                        Log.i("BudgetPlanningFragment", "Failed to create required solution: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(Call<Long> call, Throwable t) {
                Log.e("YourTag", "Network error", t);
                Toast.makeText(getContext(), "Network error occurred.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}