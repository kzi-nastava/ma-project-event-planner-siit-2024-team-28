package com.eventplanner.fragments.categories;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.eventplanner.R;
import com.eventplanner.databinding.FragmentCategoriesOverviewBinding;
import com.eventplanner.databinding.FragmentCategoryCreationBinding;
import com.eventplanner.model.enums.RequestStatus;
import com.eventplanner.model.requests.solutionCategories.CreateSolutionCategoryRequest;
import com.eventplanner.services.SolutionCategoryService;
import com.eventplanner.utils.HttpUtils;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryCreationFragment extends Fragment {
    FragmentCategoryCreationBinding binding;
    SolutionCategoryService categoryService;

    public CategoryCreationFragment() {
        // Required empty public constructor
    }

    public static CategoryCreationFragment newInstance() {
        CategoryCreationFragment fragment = new CategoryCreationFragment();
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
        binding = FragmentCategoryCreationBinding.inflate(inflater, container,false);
        View view = binding.getRoot();

        binding.buttonCreateCategory.setOnClickListener(v -> {
            createCategory();
        });

        return view;
    }

    private void createCategory() {
        String categoryName = binding.editTextName.getText().toString().trim();
        String categoryDescription = binding.editTextDescription.getText().toString().trim();

        // Validation :)
        if (categoryName.isEmpty() || categoryDescription.isEmpty()) {
            Toast.makeText(getContext(), "You cant leave fields empty", Toast.LENGTH_SHORT).show();
            return;
        }

        CreateSolutionCategoryRequest newCategory = new CreateSolutionCategoryRequest(categoryName,categoryDescription);

        Call<Long> call = categoryService.createCategory(newCategory);
        call.enqueue(new Callback<Long>() {
            @Override
            public void onResponse(Call<Long> call, Response<Long> response) {
                if (response.isSuccessful()) {
                    Long categoryId = response.body();
                    Toast.makeText(getContext(), "Category successfully created", Toast.LENGTH_SHORT).show();
                    Log.d("CategoryCreationFragment", "Category successfully created, ID: " + categoryId);
                } else {
                    String message = "Unknown error.";
                    if (response.errorBody() != null) {
                        try {
                            String errorString = response.errorBody().string();
                            Log.e("CategoryCreationFragment", "Error body: " + errorString);
                            message = new JSONObject(errorString).optString("error", message);
                        } catch (Exception ignored) {}
                    }
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    Log.e("CategoryCreationFragment", "Error: " + message);
                }
            }

            @Override
            public void onFailure(Call<Long> call, Throwable t) {
                Toast.makeText(getContext(), "Network failure", Toast.LENGTH_SHORT).show();
                Log.e("CategoryCreationFragment", "Network failure: " + t.getMessage());
            }
        });
    }


}