package com.eventplanner.services;

import com.eventplanner.model.requests.LoginRequest;
import com.eventplanner.model.requests.RegisterBusinessOwnerRequest;
import com.eventplanner.model.requests.RegisterEventOrganizerRequest;
import com.eventplanner.model.responses.AuthResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthService {
    @POST("auth/event-organizer")
    Call<AuthResponse> registerEventOrganizer(@Body RegisterEventOrganizerRequest request);

    @POST("auth/business-owner")
    Call<AuthResponse> registerBusinessOwner(@Body RegisterBusinessOwnerRequest request);

    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);
}
