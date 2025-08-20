package com.eventplanner.services;

import com.eventplanner.model.responses.calendar.CalendarResponseDTO;

import retrofit2.Call;
import retrofit2.http.GET;

public interface CalendarService {
    
    /**
     * Get accepted events for the current user (any user type)
     * All users can have private events they have accepted displayed
     */
    @GET("calendar/accepted-events")
    Call<CalendarResponseDTO> getCurrentUserAcceptedEvents();

    /**
     * Get created events for the current event organizer
     * Event organizers have their created events on the calendar
     */
    @GET("calendar/created-events")
    Call<CalendarResponseDTO> getCurrentEventOrganizerCreatedEvents();

    /**
     * Get service reservations for the current business owner
     * Business owners have their service reservation dates on the calendar
     */
    @GET("calendar/service-reservations")
    Call<CalendarResponseDTO> getCurrentBusinessOwnerServiceReservations();
}
