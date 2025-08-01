package com.eventplanner.services;

import com.eventplanner.model.requests.events.CreateEventRequest;
import com.eventplanner.model.requests.events.UpdateEventRequest;
import com.eventplanner.model.responses.PagedResponse;
import com.eventplanner.model.responses.events.GetEventResponse;

import java.util.Collection;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface EventService {
    @GET("events/{eventId}")
    Call<GetEventResponse> getEventById(@Path("eventId") Long eventId);

    @GET("events/top-events")
    Call<PagedResponse<GetEventResponse>> getTopEvents();

    @GET("events/public")
    Call<PagedResponse<GetEventResponse>> getAllPublicEvents(
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("events/event-organizer/{eventOrganizerId}/active-events")
    Call<Collection<GetEventResponse>> getActiveEventsByOrganizer(
            @Path("eventOrganizerId") Long eventOrganizerId
    );

    @GET("events/event-organizer/{eventOrganizerId}/active-events/event-types")
    Call<Collection<GetEventResponse>> getActiveEventsByTypeAndOrganizer(
            @Path("eventOrganizerId") Long eventOrganizerId,
            @Query("eventTypeIds") Collection<Long> eventTypeIds
    );

    @POST("events")
    Call<Void> createEvent(@Body CreateEventRequest createEventRequest);

    @PUT("events/{eventId}")
    Call<Void> updateEvent(@Path("eventId") Long eventId, @Body UpdateEventRequest updateEventRequest);

    @DELETE("events/{eventId}")
    Call<Void> deleteEventById(@Path("eventId") Long eventId);

    @POST("events/{eventId}/favorite")
    Call<Void> toggleEventFavoriteStateForCurrentUser(@Path("eventId") Long eventId);

    @GET("events/favorites")
    Call<Collection<GetEventResponse>> getFavoriteEventsForCurrentUser();

    @GET("events/{eventId}/guests")
    Call<ResponseBody> getGuestListPdf(@Path("eventId") Long eventId);

    @GET("events/{eventId}/details")
    Call<ResponseBody> getEventDetails(@Path("eventId") Long eventId);
}
