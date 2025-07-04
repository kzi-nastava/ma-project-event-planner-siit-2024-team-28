package com.eventplanner.fragments.solutions;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.eventplanner.R;
import com.eventplanner.adapters.requiredSolutions.RequiredSolutionRecyclerViewAdapter;
import com.eventplanner.adapters.solutions.SolutionRecyclerViewAdapter;
import com.eventplanner.databinding.FragmentBudgetPlanningBinding;
import com.eventplanner.databinding.FragmentBudgetPlanningItemsBinding;
import com.eventplanner.model.responses.events.GetEventResponse;
import com.eventplanner.model.responses.requiredSolutions.GetRequiredSolutionItemResponse;
import com.eventplanner.model.responses.solutions.GetSolutionDetailsResponse;
import com.eventplanner.model.responses.solutions.GetSolutionResponse;
import com.eventplanner.services.EventService;
import com.eventplanner.services.RequiredSolutionService;
import com.eventplanner.services.SolutionService;
import com.eventplanner.utils.HttpUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BudgetPlanningItemsFragment extends Fragment {
    FragmentBudgetPlanningItemsBinding binding;
    private static final String ARG_EVENT_ID = "eventId";
    private Long eventId;
    private RequiredSolutionService requiredSolutionService;
    private EventService eventService;
    private SolutionService solutionService;
    private List<GetRequiredSolutionItemResponse> items;
    private RequiredSolutionRecyclerViewAdapter adapter;

    public BudgetPlanningItemsFragment() {
        // Required empty public constructor
    }

    public static BudgetPlanningItemsFragment newInstance(Long eventId) {
        BudgetPlanningItemsFragment fragment = new BudgetPlanningItemsFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventId = getArguments().getLong(ARG_EVENT_ID);
        }
        requiredSolutionService = HttpUtils.getRequiredSolutionService();
        eventService = HttpUtils.getEventService();
        solutionService = HttpUtils.getSolutionService();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBudgetPlanningItemsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        fetchEvent();
        setupItems();

        return view;
    }

    private void setupItems() {
        Call<Collection<GetRequiredSolutionItemResponse>> call = requiredSolutionService.getRequiredSolutionsForEvent(eventId);
        call.enqueue(new Callback<Collection<GetRequiredSolutionItemResponse>>() {
            @Override
            public void onResponse(Call<Collection<GetRequiredSolutionItemResponse>> call, Response<Collection<GetRequiredSolutionItemResponse>> response) {
                if (response.isSuccessful()) {
                    items = new ArrayList<>(response.body());
                    setupItemRecyclerView();
                    calculateMaximumBudget();
                    Log.d("BudgetPlanningItemsFragment", "Fetched required solution: " + items.size());
                } else {
                    Log.e("BudgetPlanningItemsFragment", "Failed to fetch required solution: " + response.code());
                    Toast.makeText(getContext(), "Failed to load budget data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Collection<GetRequiredSolutionItemResponse>> call, Throwable t) {
                Log.e("BudgetPlanningItemsFragment", "Network error", t);
                Toast.makeText(getContext(), "Network error occurred", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupItemRecyclerView() {
        RecyclerView itemsRecyclerView = binding.recyclerItems;
        itemsRecyclerView.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        );

        RequiredSolutionRecyclerViewAdapter.OnItemInteractionListener listener =
                new RequiredSolutionRecyclerViewAdapter.OnItemInteractionListener() {
                    @Override
                    public void onSolutionsRequested(Long categoryId, Double budget, RecyclerView recyclerView) {
                        Call<Collection<GetSolutionResponse>> call = solutionService.getAppropriateSolutions(categoryId, budget);
                        call.enqueue(new Callback<Collection<GetSolutionResponse>>() {
                            @Override
                            public void onResponse(Call<Collection<GetSolutionResponse>> call, Response<Collection<GetSolutionResponse>> response) {
                                if (response.isSuccessful()) {
                                    List<GetSolutionResponse> solutions = new ArrayList<>(response.body());
                                    SolutionRecyclerViewAdapter solutionsAdapter = new SolutionRecyclerViewAdapter(solutions);

                                    if (recyclerView.getLayoutManager() == null) {
                                        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                                    }

                                    recyclerView.setAdapter(solutionsAdapter);
                                   Log.i("BudgetPlanningItemsFragment", "Successfully fetched solutions for an item.");
                                } else {
                                    Log.i("BudgetPlanningItemsFragment", "Error while fetching solutions for an item: " + response.code());
                                }
                            }

                            @Override
                            public void onFailure(Call<Collection<GetSolutionResponse>> call, Throwable t) {
                                Log.e("BudgetPlanningFragment", "Network failure", t);
                                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onDeleteClick(int position, Long requiredSolutionId) {
                        Call<Void> call = requiredSolutionService.deleteRequiredSolution(requiredSolutionId);
                        call.enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(getContext(), "Deleted successfully", Toast.LENGTH_SHORT).show();
                                    adapter.removeItemById(requiredSolutionId);
                                    calculateMaximumBudget();
                                } else {
                                    Log.i("BudgetPlanningItemsFragment", "Delete failed: " + response.code());
                                    Toast.makeText(getContext(), "Delete failed: " + response.code(), Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Log.i("BudgetPlanningItemsFragment", "Network failure: " + t.getMessage());
                                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                };

        adapter = new RequiredSolutionRecyclerViewAdapter(items, listener);
        itemsRecyclerView.setAdapter(adapter);
    }
    private void calculateMaximumBudget() {
        Double maximumBudget = 0.0;
        for(GetRequiredSolutionItemResponse item : items)
            maximumBudget += item.getBudget();
        binding.textBudget.setText(getString(R.string.maximum_budget) + " " + String.valueOf(maximumBudget) + "$");
    }

    private void fetchEvent() {
        Call<GetEventResponse> call = eventService.getEventById(eventId);
        call.enqueue(new Callback<GetEventResponse>() {
            @Override
            public void onResponse(Call<GetEventResponse> call, Response<GetEventResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GetEventResponse event = response.body();
                    binding.textEventName.setText(binding.textEventName.getText() + " " + event.getName());
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
}