package com.eventplanner.services;

import com.eventplanner.model.responses.events.GetEventResponse;

import java.util.Collection;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface EventService {
    @GET("events/{id}")
    Call<GetEventResponse> getEventById(@Path("id") Long id);
    @GET("events/event-organizer/{eventOrganizerId}/active-events")
    Call<Collection<GetEventResponse>> getActiveEventsByOrganizer(@Path("eventOrganizerId") Long eventOrganizerId);
}
