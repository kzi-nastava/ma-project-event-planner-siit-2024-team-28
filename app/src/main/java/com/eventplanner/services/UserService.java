package com.eventplanner.services;

import com.eventplanner.model.responses.users.GetUserResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface UserService {
    @GET("users/{id}")
    Call<GetUserResponse> getUserById(@Path("id") Long id);
}
