package com.eventplanner.fragments.categories;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.eventplanner.R;
import com.eventplanner.databinding.FragmentCategoriesOverviewBinding;
import com.eventplanner.databinding.FragmentCategorizeSolutionBinding;
import com.eventplanner.model.responses.solutionCateogries.GetSolutionCategoryResponse;
import com.eventplanner.model.responses.solutions.GetSolutionResponse;
import com.eventplanner.services.SolutionCategoryService;
import com.eventplanner.services.SolutionService;
import com.eventplanner.utils.HttpUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategorizeSolutionFragment extends Fragment {
    FragmentCategorizeSolutionBinding binding;
    SolutionService solutionService;
    SolutionCategoryService categoryService;
    Long selectedSolutionId;
    Long selectedCategoryId;

    public CategorizeSolutionFragment() {
        // Required empty public constructor
    }

    public static CategorizeSolutionFragment newInstance() {
        CategorizeSolutionFragment fragment = new CategorizeSolutionFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        categoryService = HttpUtils.getSolutionCategoryService();
        solutionService = HttpUtils.getSolutionService();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCategorizeSolutionBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        fetchPendingSolutions();
        fetchCategories();

        binding.buttonCategorize.setOnClickListener(v -> {
            categorizeSolution();
        });

        return view;
    }

    private void fetchPendingSolutions() {
        solutionService.getPendingSolutions().enqueue(new Callback<Collection<GetSolutionResponse>>() {
            @Override
            public void onResponse(Call<Collection<GetSolutionResponse>> call, Response<Collection<GetSolutionResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<GetSolutionResponse> solutions = new ArrayList<>(response.body());

                    List<String> solutionNames = new ArrayList<>();
                    solutionNames.add("Select solution");  // placeholder

                    for (GetSolutionResponse sol : solutions) {
                        solutionNames.add(sol.getName());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            solutionNames
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    Spinner spinner = binding.spinnerSolution;
                    spinner.setAdapter(adapter);

                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (position == 0) {
                                selectedSolutionId = null;
                            } else {
                                GetSolutionResponse selectedSolution = solutions.get(position - 1);
                                selectedSolutionId = selectedSolution.getId();
                                Log.d("FragmentTag", "Selected solution: " + selectedSolution.getName());
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            selectedSolutionId = null;
                        }
                    });

                } else {
                    Toast.makeText(getContext(), "Failed to fetch solutions", Toast.LENGTH_SHORT).show();
                    Log.i("CategorizeSolutionFragment", "Error while fetching pending solutions: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Collection<GetSolutionResponse>> call, Throwable t) {
                Toast.makeText(getContext(), "Network failure: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void fetchCategories() {
        Call<Collection<GetSolutionCategoryResponse>> call = categoryService.getAcceptedCategories();
        call.enqueue(new Callback<Collection<GetSolutionCategoryResponse>>() {
            @Override
            public void onResponse(Call<Collection<GetSolutionCategoryResponse>> call, Response<Collection<GetSolutionCategoryResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<GetSolutionCategoryResponse> categories = new ArrayList<>(response.body());

                    List<String> categoryNames = new ArrayList<>();
                    categoryNames.add("Select category"); // placeholder
                    for (GetSolutionCategoryResponse cat : categories) {
                        categoryNames.add(cat.getName());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            categoryNames
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    Spinner spinner = binding.spinnerCategory;
                    spinner.setAdapter(adapter);

                    spinner.setSelection(0, false);

                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (position == 0) {
                                selectedCategoryId = null;
                            } else {
                                GetSolutionCategoryResponse selectedCategory = categories.get(position - 1);
                                selectedCategoryId = selectedCategory.getId();
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            selectedCategoryId = null;
                        }
                    });

                } else {
                    Toast.makeText(getContext(), "Failed to fetch categories", Toast.LENGTH_SHORT).show();
                    Log.e("CategorizeSolutionFragment", "Failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Collection<GetSolutionCategoryResponse>> call, Throwable t) {
                Log.e("CategorizeSolutionFragment", "Network failure", t);
            }
        });
    }

    private void categorizeSolution() {
        if (selectedSolutionId == null || selectedCategoryId == null) {
            Toast.makeText(getContext(), "You have to select solution and category.", Toast.LENGTH_SHORT).show();
            return;
        }

        categoryService.categorizeSolution(selectedCategoryId, selectedSolutionId)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Solution categorized successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Failed to categorize solution.", Toast.LENGTH_SHORT).show();
                            Log.i("CategorizeSolutionFragment", "Error while categorizing soluiton: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(getContext(), "Network failure." , Toast.LENGTH_SHORT).show();
                        Log.i("CategorizeSolutionFragment", "Network failure " + t.getMessage());
                    }
                });
    }

}