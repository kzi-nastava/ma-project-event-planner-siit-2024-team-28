package com.eventplanner.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.eventplanner.BuildConfig;
import com.eventplanner.services.AuthService;
import com.eventplanner.services.EventTypeService;
import com.eventplanner.services.SolutionCategoryService;
import com.eventplanner.services.SolutionService;
import com.eventplanner.services.UserService;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class HttpUtils {
    private static final String BACKEND_BASE_URL = BuildConfig.BACKEND_BASE_URL; //BuildConfig.BACKEND_BASE_URL;
    private static final String PREF_NAME = "authPrefs";
    private static final String TOKEN_KEY = "authToken";

    private static Retrofit retrofit;

    public static void initialize(Context context) {
        JwtInterceptor jwtInterceptor = new JwtInterceptor(() -> {
            SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            return sharedPreferences.getString(TOKEN_KEY, null);
        });

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(jwtInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BACKEND_BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static AuthService getAuthService() {
        return retrofit.create(AuthService.class);
    }
    public static SolutionService getSolutionService() { return retrofit.create(SolutionService.class); }
    public static SolutionCategoryService getSolutionCategoryService() { return retrofit.create(SolutionCategoryService.class); }
    public static UserService getUserService() { return retrofit.create(UserService.class); }
    public static EventTypeService getEventTypeService() { return retrofit.create(EventTypeService.class); }
}
