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
import com.eventplanner.databinding.FragmentCategoryCreationBinding;
import com.eventplanner.databinding.FragmentCategoryEditBinding;
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


public class CategoryEditFragment extends Fragment {
    SolutionCategoryService categoryService;
    FragmentCategoryEditBinding binding;
    private static final String ARG_CATEGORY_ID = "categoryId";
    private static Long categoryId;

    public CategoryEditFragment() {
        // Required empty public constructor
    }

    public static CategoryEditFragment newInstance() {
        CategoryEditFragment fragment = new CategoryEditFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_CATEGORY_ID, categoryId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        categoryService = HttpUtils.getSolutionCategoryService();
        if(getArguments() != null) {
            categoryId = getArguments().getLong(ARG_CATEGORY_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCategoryEditBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        fetchCategory();

        binding.buttonEditCategory.setOnClickListener(v -> {
            editCategory();
        });

        binding.buttonDeleteCategory.setOnClickListener(v -> {
            deleteCategory();
        });

        return view;
    }

    private void fetchCategory() {
        Call<GetSolutionCategoryResponse> call = categoryService.getSolutionCategoryById(categoryId);
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

    private void editCategory() {
        String categoryName = binding.editTextName.getText().toString().trim();
        String categoryDescription = binding.editTextDescription.getText().toString().trim();
        if(categoryName.isEmpty() || categoryDescription.isEmpty()) {
            Toast.makeText(getContext(), "Fields cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        UpdateSolutionCategoryRequest updateRequest = new UpdateSolutionCategoryRequest(categoryName,categoryDescription);
        Call<Void> call = categoryService.updateCategory(categoryId, updateRequest);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Category edited successfully.", Toast.LENGTH_SHORT).show();
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
    private void deleteCategory() {
        Call<Void> call = categoryService.deleteCategory(categoryId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Category deleted successfully.", Toast.LENGTH_SHORT).show();
                } else {
                    String message = "Unknown error.";
                    if (response.errorBody() != null) {
                        try {
                            String errorString = response.errorBody().string();
                            Log.e("PriceListFragment", "Error body: " + errorString);
                            message = new JSONObject(errorString).optString("error", message);
                        } catch (Exception ignored) {}
                    }
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    Log.e("PriceListFragment", "Error: " + message);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Network failure: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}