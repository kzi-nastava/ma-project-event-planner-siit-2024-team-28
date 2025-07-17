package com.eventplanner.services;

import com.eventplanner.model.requests.solutionCategories.CategoryAcceptionRequest;
import com.eventplanner.model.requests.solutionCategories.CreatePendingCategoryRequest;
import com.eventplanner.model.requests.solutionCategories.CreateSolutionCategoryRequest;
import com.eventplanner.model.requests.solutionCategories.UpdateSolutionCategoryRequest;
import com.eventplanner.model.responses.solutionCateogries.GetSolutionCategoryResponse;

import java.util.Collection;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SolutionCategoryService {
    @POST("categories")
    Call<Long> createCategory(@Body CreateSolutionCategoryRequest request);

    @POST("categories/pending-category")
    Call<Long> createPendingCategory(@Body CreatePendingCategoryRequest request);

    @GET("categories/{id}")
    Call<GetSolutionCategoryResponse> getSolutionCategoryById(@Path("id") Long id);

    @GET("categories")
    Call<Collection<GetSolutionCategoryResponse>> getAllSolutionCategories();

    @GET("categories/accepted-categories")
    Call<Collection<GetSolutionCategoryResponse>> getAcceptedCategories();

    @GET("categories/pending-categories")
    Call<Collection<GetSolutionCategoryResponse>> getPendingCategories();

    @PUT("categories/{id}")
    Call<Void> updateCategory(@Path("id") Long id, @Body UpdateSolutionCategoryRequest request);

    @PUT("categories/category-acception")
    Call<Void> categoryAcception(@Body CategoryAcceptionRequest request);

    @DELETE("categories/{id}")
    Call<Void> deleteCategory(@Path("id") Long id);

    @PUT("categories/categorize-solution")
    Call<Void> categorizeSolution(
            @Query("categoryId") Long categoryId,
            @Query("solutionId") Long solutionId
    );
}
