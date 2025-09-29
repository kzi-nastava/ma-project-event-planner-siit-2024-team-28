package com.eventplanner.services;

import com.eventplanner.model.responses.PagedResponse;
import com.eventplanner.model.responses.notifications.GetNotificationCountResponse;
import com.eventplanner.model.responses.notifications.GetNotificationResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface NotificationService {

    @GET("notifications/user/{userId}")
    Call<PagedResponse<GetNotificationResponse>> getUserNotifications(@Path("userId") Long userId, @Query("page") int page,
                                                                      @Query("size") int size);

    @GET("notifications/user/{userId}/unread")
    Call<List<GetNotificationResponse>> getUnreadNotifications(@Path("userId") Long userId);

    @GET("notifications/user/{userId}/count")
    Call<GetNotificationCountResponse> getNotificationCount(@Path("userId") Long userId);

    @PUT("notifications/{notificationId}/read")
    Call<Void> markAsRead(@Path("notificationId") Long notificationId, @Query("userId") Long userId);

    @PUT("notifications/user/{userId}/read-all")
    Call<Void> markAllAsRead(@Path("userId") Long userId);
}
