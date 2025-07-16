package com.eventplanner.services;

import com.eventplanner.model.requests.eventTypes.CreateEventTypeRequest;
import com.eventplanner.model.requests.eventTypes.UpdateEventTypeRequest;
import com.eventplanner.model.responses.eventTypes.GetEventTypeResponse;
import com.eventplanner.model.responses.solutionCateogries.GetSolutionCategoryResponse;

import java.util.Collection;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface EventTypeService {
    @GET("event-types")
    Call<Collection<GetEventTypeResponse>> getAllEventTypes();

    @GET("event-types/active")
    Call<Collection<GetEventTypeResponse>> getActiveEventTypes();

    @GET("event-types/{id}")
    Call<GetEventTypeResponse> getEventTypeById(@Path("id") Long id);

    @POST("event-types")
    Call<GetEventTypeResponse> createEventType(@Body CreateEventTypeRequest request);

    @PUT("event-types/{id}")
    Call<GetEventTypeResponse> updateEventType(@Path("id") Long id, @Body UpdateEventTypeRequest request);

    @PATCH("event-types/{id}/activate")
    Call<Void> activateEventType(@Path("id") Long id);

    @PATCH("event-types/{id}/deactivate")
    Call<Void> deactivateEventType(@Path("id") Long id);

    @GET("event-types/recommended-categories/{id}")
    Call<List<GetSolutionCategoryResponse>> getRecommendedCategories(@Path("id") Long eventTypeId);
}