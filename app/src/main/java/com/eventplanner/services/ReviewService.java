package com.eventplanner.services;

import com.eventplanner.model.responses.reviews.GetReviewPreviewResponse;

import java.util.Collection;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ReviewService {
    @GET("reviews/business-owner/{businessOwnerId}")
    Call<Collection<GetReviewPreviewResponse>> getAllReviewsByBusinessOwnerId(@Path("businessOwnerId") Long businessOwnerId);
}
