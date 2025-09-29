package com.eventplanner.services;

import com.eventplanner.model.requests.eventReviews.CreateEventReviewRequest;
import com.eventplanner.model.requests.eventReviews.UpdateEventReviewRequest;
import com.eventplanner.model.responses.eventReviews.GetEventReviewResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface EventReviewService {
    @POST("events/reviews")
    Call<GetEventReviewResponse> createReview(@Body CreateEventReviewRequest request);

    @PUT("events/reviews/{id}")
    Call<GetEventReviewResponse> updateReview(@Path("id") Long id, @Body UpdateEventReviewRequest request);

    @DELETE("events/reviews/{id}")
    Call<Void> deleteReview(@Path("id") Long id);

    @GET("events/reviews/event-organizer/event")
    Call<GetEventReviewResponse> getReviewByUserAndEvent(@Query("userId") Long userId, @Query("eventId") Long eventId);
}
