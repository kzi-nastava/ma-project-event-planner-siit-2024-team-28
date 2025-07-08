package com.eventplanner.services;

import com.eventplanner.model.requests.reviews.CreateReviewRequest;
import com.eventplanner.model.responses.reviews.GetReviewPreviewResponse;
import com.eventplanner.model.responses.reviews.GetReviewResponse;

import java.util.Collection;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ReviewService {
    @POST("reviews")
    Call<GetReviewResponse> createReview(@Body CreateReviewRequest review);
    @GET("reviews/business-owner/{businessOwnerId}")
    Call<Collection<GetReviewPreviewResponse>> getAllReviewsByBusinessOwnerId(@Path("businessOwnerId") Long businessOwnerId);
}
