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
import com.eventplanner.databinding.FragmentCategoryAcceptionBinding;
import com.eventplanner.model.enums.RequestStatus;
import com.eventplanner.model.requests.solutionCategories.CategoryAcceptionRequest;
import com.eventplanner.model.requests.solutionCategories.UpdateSolutionCategoryRequest;
import com.eventplanner.model.responses.solutionCateogries.GetSolutionCategoryResponse;
import com.eventplanner.services.SolutionCategoryService;
import com.eventplanner.utils.HttpUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryAcceptionFragment extends Fragment {
    FragmentCategoryAcceptionBinding binding;
    SolutionCategoryService categoryService;
    Long selectedCategoryId;

    public CategoryAcceptionFragment() {
        // Required empty public constructor
    }

    public static CategoryAcceptionFragment newInstance(String param1, String param2) {
        CategoryAcceptionFragment fragment = new CategoryAcceptionFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        categoryService = HttpUtils.getSolutionCategoryService();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCategoryAcceptionBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        fetchPendingCategories();

        binding.buttonAcceptCategory.setOnClickListener(v -> {
            acceptCategory();
        });
        binding.buttonRejectCategory.setOnClickListener(v -> {
            rejectCategory();
        });

        return view;
    }

    private void fetchPendingCategories() {
        Call<Collection<GetSolutionCategoryResponse>> call = categoryService.getPendingCategories();
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

                    Spinner spinner = binding.spinnerCategories;
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
                                fetchCategory();
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            selectedCategoryId = null;
                        }
                    });

                } else {
                    Toast.makeText(getContext(), "Failed to fetch categories", Toast.LENGTH_SHORT).show();
                    Log.e("CategoryAcceptionFragment", "Failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Collection<GetSolutionCategoryResponse>> call, Throwable t) {
                Log.e("CategoryAcceptionFragment", "Network failure", t);
            }
        });
    }

    private void fetchCategory() {
        Call<GetSolutionCategoryResponse> call = categoryService.getSolutionCategoryById(selectedCategoryId);
        call.enqueue(new Callback<GetSolutionCategoryResponse>() {
            @Override
            public void onResponse(Call<GetSolutionCategoryResponse> call, Response<GetSolutionCategoryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GetSolutionCategoryResponse category = response.body();
                    binding.editTextName.setText(category.getName());
                    binding.editTextDescription.setText(category.getDescription());
                    Log.d("CategoryEditFragment", "Fetched category with id: " + category.getId());
                } else {
                    Log.e("CategoryEditFragment", "Failed to fetch. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GetSolutionCategoryResponse> call, Throwable t) {
                Log.e("CategoryEditFragment", "Network error", t);
            }
        });
    }

    private void acceptCategory() {
        if(selectedCategoryId == null) {
            Toast.makeText(getContext(), "You have to select category.", Toast.LENGTH_SHORT).show();
            return;
        }
        String categoryName = binding.editTextName.getText().toString().trim();
        String categoryDescription = binding.editTextDescription.getText().toString().trim();
        if(categoryName.isEmpty() || categoryDescription.isEmpty()) {
            Toast.makeText(getContext(), "Fields cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        CategoryAcceptionRequest acceptionRequest = new CategoryAcceptionRequest(selectedCategoryId, categoryName,categoryDescription, RequestStatus.ACCEPTED);
        Call<Void> call = categoryService.categoryAcception(acceptionRequest);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Category accepted successfully.", Toast.LENGTH_SHORT).show();
                } else {
                    String message = "Unknown error.";
                    if (response.errorBody() != null) {
                        try {
                            String errorString = response.errorBody().string();
                            Log.e("CategoryAcceptionFragment", "Error body: " + errorString);
                            message = new JSONObject(errorString).optString("error", message);
                        } catch (Exception ignored) {}
                    }
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    Log.e("CategoryAcceptionFragment", "Error: " + message);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Network failure: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void rejectCategory() {
        if(selectedCategoryId == null) {
            Toast.makeText(getContext(), "You have to select category.", Toast.LENGTH_SHORT).show();
            return;
        }
        String categoryName = binding.editTextName.getText().toString().trim();
        String categoryDescription = binding.editTextDescription.getText().toString().trim();
        if(categoryName.isEmpty() || categoryDescription.isEmpty()) {
            Toast.makeText(getContext(), "Fields cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        CategoryAcceptionRequest acceptionRequest= new CategoryAcceptionRequest(selectedCategoryId, categoryName,categoryDescription, RequestStatus.REJECTED);
        Call<Void> call = categoryService.categoryAcception(acceptionRequest);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Category rejected successfully.", Toast.LENGTH_SHORT).show();
                } else {
                    String message = "Unknown error.";
                    if (response.errorBody() != null) {
                        try {
                            String errorString = response.errorBody().string();
                            Log.e("CategoryAcceptionFragment", "Error body: " + errorString);
                            message = new JSONObject(errorString).optString("error", message);
                        } catch (Exception ignored) {}
                    }
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    Log.e("CategoryAcceptionFragment", "Error: " + message);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Network failure: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}