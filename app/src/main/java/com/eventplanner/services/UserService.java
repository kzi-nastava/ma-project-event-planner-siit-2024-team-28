package com.eventplanner.services;

import com.eventplanner.model.responses.users.GetUserResponse;

import retrofit2.Call;
import retrofit2.http.GET;
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
}
