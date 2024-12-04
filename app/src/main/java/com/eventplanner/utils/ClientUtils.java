package com.eventplanner.utils;

import com.eventplanner.services.AuthService;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ClientUtils {
    private static final String BASE_URL = "http://192.168.1.7:8080/api/";

    public static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    public static AuthService authService = retrofit.create(AuthService.class);
}
