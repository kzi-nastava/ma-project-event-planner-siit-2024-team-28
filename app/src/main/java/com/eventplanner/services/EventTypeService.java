package com.eventplanner.services;

import com.eventplanner.model.responses.eventTypes.GetEventTypeResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface EventTypeService {
    @GET("/event-types/{id}")
    Call<GetEventTypeResponse> getEventTypeById(@Path("id") Long id);
}
