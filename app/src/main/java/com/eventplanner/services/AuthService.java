package com.eventplanner.services;

import com.eventplanner.model.requests.auth.LoginRequest;
import com.eventplanner.model.requests.auth.RegisterBusinessOwnerRequest;
import com.eventplanner.model.requests.auth.RegisterEventOrganizerRequest;
import com.eventplanner.model.responses.auth.AuthResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthService {
    @POST("auth/event-organizer")
    Call<Void> registerEventOrganizer(@Body RegisterEventOrganizerRequest request);

    @POST("auth/business-owner")
    Call<Void> registerBusinessOwner(@Body RegisterBusinessOwnerRequest request);

    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);
}
