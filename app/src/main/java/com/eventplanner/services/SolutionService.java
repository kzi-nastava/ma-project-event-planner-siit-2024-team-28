package com.eventplanner.services;

import com.eventplanner.model.responses.solutions.GetPriceListSolutionResponse;
import com.eventplanner.model.responses.solutions.GetSolutionDetailsResponse;
import com.eventplanner.model.responses.solutions.GetSolutionResponse;

import java.util.Collection;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SolutionService {
    @GET("solutions/{id}")
    Call<GetSolutionResponse> getSolutionById(@Path("id") Long id);

    @GET("solutions/solution-details/{id}")
    Call<GetSolutionDetailsResponse> getSolutionDetailsById(@Path("id") Long id);

    @GET("solutions/price-list")
    Call<Collection<GetPriceListSolutionResponse>> getPriceList(@Query("businessOwnerId") Long businessOwnerId);

    @PATCH("solutions/{id}/price")
    Call<Void> updateSolutionPrice(@Path("id") Long solutionId, @Query("newPrice") Double newPrice);

    @PATCH("solutions/{id}/discount")
    Call<Void> updateSolutionDiscount(@Path("id") Long solutionId, @Query("newDiscount") Double newDiscount);

    @GET("solutions/pending-solutions")
    Call<Collection<GetSolutionResponse>> getPendingSolutions();

    @GET("solutions/required-solution/appropriate-solutions")
    Call<Collection<GetSolutionResponse>> getAppropriateSolutions(
            @Query("categoryId") Long categoryId,
            @Query("eventTypeId") Long eventTypeId,
            @Query("amount") Double amount
    );

    @GET("/api/solutions/{solutionId}/can-comment-review")
    Call<Boolean> canUserCommentReview(
            @Path("solutionId") Long solutionId,
            @Query("userId") Long userId
    );

    @GET("solutions/favorites")
    Call<Collection<GetSolutionResponse>> getFavoriteSolutionsForCurrentUser();
}
