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

import com.eventplanner.R;
import com.eventplanner.adapters.solutionCategories.CategoryListAdapter;
import com.eventplanner.databinding.FragmentCategoriesOverviewBinding;
import com.eventplanner.model.responses.solutionCateogries.GetSolutionCategoryResponse;
import com.eventplanner.services.SolutionCategoryService;
import com.eventplanner.utils.HttpUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoriesOverviewFragment extends Fragment {
    FragmentCategoriesOverviewBinding binding;
    SolutionCategoryService categoryService;

    public CategoriesOverviewFragment() {
        // Required empty public constructor
    }

    public static CategoriesOverviewFragment newInstance(String param1, String param2) {
        CategoriesOverviewFragment fragment = new CategoriesOverviewFragment();
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
        binding = FragmentCategoriesOverviewBinding.inflate(inflater, container,false);
        View view = binding.getRoot();

        fetchCategories();

        return view;
    }

    private void fetchCategories() {
        Call<Collection<GetSolutionCategoryResponse>> call = categoryService.getAcceptedCategories();
        call.enqueue(new Callback<Collection<GetSolutionCategoryResponse>>() {
            @Override
            public void onResponse(Call<Collection<GetSolutionCategoryResponse>> call, Response<Collection<GetSolutionCategoryResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<GetSolutionCategoryResponse> categories = new ArrayList<>(response.body());
                    CategoryListAdapter adapter = new CategoryListAdapter(getContext(), categories);
                    binding.categoryListView.setAdapter(adapter);
                } else {
                    Log.e("CategoriesOverviewFragment", "Failed to fetch categories: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Collection<GetSolutionCategoryResponse>> call, Throwable t) {
                Log.e("CategoriesOverviewFragment", "Network failure", t);
            }
        });
    }
}