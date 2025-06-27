package com.eventplanner.services;

import com.eventplanner.model.responses.solutionCateogries.GetSolutionCategoryResponse;

import java.util.Collection;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface SolutionCategoryService {
    @GET("categories/{id}")
    Call<GetSolutionCategoryResponse> getSolutionCategoryById(@Path("id") Long id);

    @GET("categories")
    Call<Collection<GetSolutionCategoryResponse>> getAllSolutionCategories();
}
