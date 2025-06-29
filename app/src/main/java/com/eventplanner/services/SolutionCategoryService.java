package com.eventplanner.services;

import com.eventplanner.model.requests.solutionCategories.CreateSolutionCategoryRequest;
import com.eventplanner.model.responses.solutionCateogries.GetSolutionCategoryResponse;

import java.util.Collection;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface SolutionCategoryService {
    @POST("categories")
    Call<Long> createCategory(@Body CreateSolutionCategoryRequest request);

    @GET("categories/{id}")
    Call<GetSolutionCategoryResponse> getSolutionCategoryById(@Path("id") Long id);

    @GET("categories")
    Call<Collection<GetSolutionCategoryResponse>> getAllSolutionCategories();

    @GET("categories/accepted-categories")
    Call<Collection<GetSolutionCategoryResponse>> getAcceptedCategories();
}
