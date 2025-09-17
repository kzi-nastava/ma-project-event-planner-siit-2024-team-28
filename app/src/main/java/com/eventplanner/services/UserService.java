package com.eventplanner.services;

import com.eventplanner.model.requests.users.UpdateBusinessOwnerRequest;
import com.eventplanner.model.requests.users.UpdateEventOrganizerRequest;
import com.eventplanner.model.requests.users.UpdatePasswordRequest;
import com.eventplanner.model.requests.users.UpdateUserRequest;
import com.eventplanner.model.responses.users.GetUserProfilePictureResponse;
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
    Call<GetUserProfilePictureResponse> getUserProfilePictureBase64(@Path("id") Long id);

    @PUT("users/business-owners/{id}")
    Call<Void> updateBusinessOwner(@Path("id") Long id, @Body UpdateBusinessOwnerRequest request);

    @PUT("users/event-organizers/{id}")
    Call<Void> updateEventOrganizer(@Path("id") Long id, @Body UpdateEventOrganizerRequest request);

    @PUT("users/{id}")
    Call<Void> updateUser(@Path("id") Long id, @Body UpdateUserRequest request);

    @PATCH("users/password")
    Call<Void> updateUserPassword(@Body UpdatePasswordRequest request);

    @GET("users/{id}/can-be-deactivated")
    Call<Boolean> canUserBeDeactivated(@Path("id") Long userId);

    @DELETE("users/current/deactivate")
    Call<Void> deactivateCurrentUserAccount();

    @GET("users/block-user")
    Call <Boolean> isUserBlocked(@Query("userId") Long userId, @Query("blockedUserId") Long blockedUserId);

    @PATCH("users/block-user")
    Call <Void> blockUser(@Query("userId") Long userId, @Query("blockedUserId") Long blockedUserId);
    @PATCH("users/unblock-user")
    Call <Void> unblockUser(@Query("userId") Long userId, @Query("blockedUserId") Long blockedUserId);
}
