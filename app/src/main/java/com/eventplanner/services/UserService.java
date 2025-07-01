package com.eventplanner.services;

import com.eventplanner.model.requests.auth.UpdateBusinessOwnerRequest;
import com.eventplanner.model.requests.auth.UpdateEventOrganizerRequest;
import com.eventplanner.model.requests.auth.UpdatePasswordRequest;
import com.eventplanner.model.responses.users.GetUserResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface UserService {
    @GET("users/{id}")
    Call<GetUserResponse> getUserById(@Path("id") Long id);

    @PUT("users/favorite-service/{serviceId}")
    Call<String> favoriteService(
            @Path("serviceId") Long serviceId,
            @Query("userId") Long userId
    );

    @GET("users/{id}/profile-picture")
    Call<String> getUserProfilePictureBase64(@Path("id") Long id);

    @PUT("users/business-owners/{id}")
    Call<Void> updateBusinessOwner(@Path("id") Long id, @Body UpdateBusinessOwnerRequest request);

    @PUT("users/event-organizers/{id}")
    Call<Void> updateEventOrganizer(@Path("id") Long id, @Body UpdateEventOrganizerRequest request);

    @PATCH("users/{id}/password")
    Call<Void> updateUserPassword(@Path("id") Long id, @Body UpdatePasswordRequest request);

    @GET("users/{id}/can-be-deactivated")
    Call<Boolean> canUserBeDeactivated(@Path("id") Long userId);

    @DELETE("users/current/deactivate")
    Call<Void> deactivateCurrentUserAccount();
}
