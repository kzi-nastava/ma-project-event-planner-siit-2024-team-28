package com.eventplanner.fragments.solutions;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
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
import com.eventplanner.model.requests.requiredSolutions.UpdateRequiredSolutionRequest;
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
    private NavController navController;

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
        navController = Navigation.findNavController(getActivity(), R.id.fragment_nav_content_main);
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

    // Two RecyclerViews -> one (horizontal) for RequiredSolutions
    //                   -> second (vertical) for Solutions which is found inside first one
    // This function handles setting up those RecyclerViews and their adapters
    // Function includes overriding methods for those adapters (listener interfaces) and backend calls for those methods
    // Probably needs refactoring (understatement)
    // GL!
    private void setupItemRecyclerView() {
        RecyclerView itemsRecyclerView = binding.recyclerItems;
        itemsRecyclerView.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        // Overriding listener methods for requiredSolutionAdapter
        RequiredSolutionRecyclerViewAdapter.OnItemInteractionListener listener =
                new RequiredSolutionRecyclerViewAdapter.OnItemInteractionListener() {
                    @Override
                    public void onSolutionsRequested(GetRequiredSolutionItemResponse item, RecyclerView recyclerView) {
                        // If item has no bought solution load all possible solution user can buy
                        if (item.getSolutionId() == null) {
                            Long categoryId = item.getCategoryId();
                            Double budget = item.getBudget();
                            Call<Collection<GetSolutionResponse>> call = solutionService.getAppropriateSolutions(categoryId, budget);
                            call.enqueue(new Callback<Collection<GetSolutionResponse>>() {
                                @Override
                                public void onResponse(Call<Collection<GetSolutionResponse>> call, Response<Collection<GetSolutionResponse>> response) {
                                    if (response.isSuccessful()) {
                                        List<GetSolutionResponse> solutions = new ArrayList<>(response.body());
                                        // Overriding listener method for solution adapter
                                        SolutionRecyclerViewAdapter.OnItemClickListener solutionAdapterListener =
                                                new SolutionRecyclerViewAdapter.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(GetSolutionResponse solution) {
                                                Bundle args = new Bundle();
                                                args.putString("solutionId", String.valueOf(solution.getId()));
                                                navController.navigate(R.id.action_budget_planning_items_to_solution_details, args);
                                            }
                                        };
                                        SolutionRecyclerViewAdapter solutionsAdapter = new SolutionRecyclerViewAdapter(solutions, solutionAdapterListener);

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
                        } else {
                            // Item has a bought solution so we just load it nothing more
                            Long solutionId = item.getSolutionId();
                            Call<GetSolutionResponse> call = solutionService.getSolutionById(solutionId);
                            call.enqueue(new Callback<GetSolutionResponse>() {
                                @Override
                                public void onResponse(Call<GetSolutionResponse> call, Response<GetSolutionResponse> response) {
                                    if (response.isSuccessful()) {
                                        GetSolutionResponse fetchedSolution = response.body();
                                        List<GetSolutionResponse> solutions = new ArrayList<>();
                                        solutions.add(fetchedSolution);
                                        // Overriding listener method for solution adapter
                                        SolutionRecyclerViewAdapter.OnItemClickListener solutionAdapterListener =
                                                new SolutionRecyclerViewAdapter.OnItemClickListener() {
                                                    @Override
                                                    public void onItemClick(GetSolutionResponse solution) {
                                                        Bundle args = new Bundle();
                                                        args.putString("solutionId", String.valueOf(solution.getId()));
                                                        navController.navigate(R.id.action_budget_planning_items_to_solution_details, args);
                                                    }
                                                };
                                        SolutionRecyclerViewAdapter solutionsAdapter = new SolutionRecyclerViewAdapter(solutions, solutionAdapterListener);

                                        if (recyclerView.getLayoutManager() == null) {
                                            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                                        }

                                        recyclerView.setAdapter(solutionsAdapter);
                                        Log.i("BudgetPlanningItemsFragment", "Successfully fetched solution for an item.");
                                    } else {
                                        Log.i("BudgetPlanningItemsFragment", "Error while fetching solution for an item: " + response.code());
                                    }
                                }

                                @Override
                                public void onFailure(Call<GetSolutionResponse> call, Throwable t) {
                                    Log.e("BudgetPlanningFragment", "Network failure", t);
                                    Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
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

                    @Override
                    public void onEditClick(Long requiredSolutionId, String newBudget) {
                        Double budget = null;
                        try {
                            budget = Double.parseDouble(newBudget);
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Invalid budget input.", Toast.LENGTH_SHORT).show();
                        }
                        UpdateRequiredSolutionRequest request = new UpdateRequiredSolutionRequest(budget,null,null,null);

                        Call<Void> call = requiredSolutionService.updateRequiredSolution(requiredSolutionId, request);
                        Double finalBudget = budget;
                        call.enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    GetRequiredSolutionItemResponse item = adapter.getItemById(requiredSolutionId);
                                    if(item != null) {
                                        item.setBudget(finalBudget);
                                        int position = adapter.getPositionById(requiredSolutionId);
                                        if (position != -1) {
                                            adapter.notifyItemChanged(position);
                                        }
                                    }
                                    calculateMaximumBudget();
                                    Toast.makeText(getContext(), "Successfully updated!", Toast.LENGTH_SHORT).show();
                                    Log.i("BudgetPlanningItemsFragment", "Item's amount successfully updated.");
                                } else {
                                    Toast.makeText(getContext(), "Update failed: " + response.code(), Toast.LENGTH_SHORT).show();
                                    Log.i("BudgetPlanningItemsFragment", "Update failed: " + response.code());
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.i("BudgetPlanningItemsFragment", "Network error: " + t.getMessage());
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