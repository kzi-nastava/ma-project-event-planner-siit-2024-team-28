package com.eventplanner.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.eventplanner.BuildConfig;
import com.eventplanner.adapters.typeAdapters.LocalDateAdapter;
import com.eventplanner.adapters.typeAdapters.LocalDateTimeAdapter;
import com.eventplanner.services.AuthService;
import com.eventplanner.services.CalendarService;
import com.eventplanner.services.ChatMessageService;
import com.eventplanner.services.ChatService;
import com.eventplanner.services.EventReviewService;
import com.eventplanner.services.NotificationService;
import com.eventplanner.services.SolutionCommentService;
import com.eventplanner.services.EventService;
import com.eventplanner.services.EventTypeService;
import com.eventplanner.services.ProductService;
import com.eventplanner.services.ReportService;
import com.eventplanner.services.RequiredSolutionService;
import com.eventplanner.services.SolutionHistoryService;
import com.eventplanner.services.SolutionReviewService;
import com.eventplanner.services.ServiceService;
import com.eventplanner.services.SolutionCategoryService;
import com.eventplanner.services.SolutionService;
import com.eventplanner.services.UserService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(jwtInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BACKEND_BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    public static AuthService getAuthService() {
        return retrofit.create(AuthService.class);
    }

    public static SolutionService getSolutionService() {
        return retrofit.create(SolutionService.class);
    }

    public static SolutionCategoryService getSolutionCategoryService() {
        return retrofit.create(SolutionCategoryService.class);
    }

    public static UserService getUserService() {
        return retrofit.create(UserService.class);
    }

    public static EventTypeService getEventTypeService() {
        return retrofit.create(EventTypeService.class);
    }

    public static ReportService getReportService() {
        return retrofit.create(ReportService.class);
    }

    public static SolutionCommentService getCommentService() {
        return retrofit.create(SolutionCommentService.class);
    }

    public static SolutionReviewService getReviewService() {
        return retrofit.create(SolutionReviewService.class);
    }

    public static ServiceService getServiceService() {
        return retrofit.create(ServiceService.class);
    }

    public static EventService getEventService() {
        return retrofit.create(EventService.class);
    }

    public static RequiredSolutionService getRequiredSolutionService() {
        return retrofit.create(RequiredSolutionService.class);
    }

    public static ProductService getProductService() {
        return retrofit.create(ProductService.class);
    }

    public static ChatService getChatService() {
        return retrofit.create(ChatService.class);
    }

    public static ChatMessageService getChatMessageService() {
        return retrofit.create(ChatMessageService.class);
    }

    public static EventReviewService getEventReviewService() {
        return retrofit.create(EventReviewService.class);
    }

    public static CalendarService getCalendarService() {
        return retrofit.create(CalendarService.class);
    }

    public static SolutionHistoryService getSolutionHistoryService() {
        return retrofit.create(SolutionHistoryService.class);
    }

    public static NotificationService getNotificationService()
    {
        return retrofit.create(NotificationService.class);
    }
}
