package com.eventplanner.services;

import com.eventplanner.model.requests.solutionReviews.CreateSolutionReviewRequest;
import com.eventplanner.model.responses.solutionReviews.GetSolutionReviewPreviewResponse;
import com.eventplanner.model.responses.solutionReviews.GetSolutionReviewResponse;

import java.util.Collection;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface SolutionReviewService {
    @POST("solutions/reviews")
    Call<GetSolutionReviewResponse> createReview(@Body CreateSolutionReviewRequest review);
    @GET("solutions/reviews/business-owner/{businessOwnerId}")
    Call<Collection<GetSolutionReviewPreviewResponse>> getAllReviewsByBusinessOwnerId(@Path("businessOwnerId") Long businessOwnerId);
}
