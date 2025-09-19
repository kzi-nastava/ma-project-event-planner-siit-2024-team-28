package com.eventplanner.utils;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.eventplanner.model.responses.PagedResponse;
import com.eventplanner.model.responses.notifications.GetNotificationCountResponse;
import com.eventplanner.model.responses.notifications.GetNotificationResponse;
import com.eventplanner.services.NotificationService;
import com.eventplanner.services.NotificationWebSocketService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationManager {
    private static NotificationManager instance;
    private final NotificationWebSocketService wsService;
    private final NotificationService notificationService;
    private long currentUserId;
    private final MutableLiveData<List<GetNotificationResponse>> notificationsLiveData = new MutableLiveData<>();
    private List<GetNotificationResponse> notifications = new ArrayList<>();
    private final MutableLiveData<Integer> unreadCountLiveData = new MutableLiveData<>();
    private Gson gson;


    private NotificationManager(Context context) {
        notificationService = HttpUtils.getNotificationService();
        wsService = new NotificationWebSocketService();
        currentUserId = AuthUtils.getUserId(context);
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>)
                        (json, type, jsonDeserializationContext) -> LocalDateTime.parse(json.getAsString()))
                .create();

        if (AuthUtils.getToken(context) != null) {
            subscribeToNotifications();
            loadInitialNotifications();
            loadNotificationCount();
        }
    }

    public LiveData<List<GetNotificationResponse>> getNotificationsLiveData() {
        return notificationsLiveData;
    }

    public static synchronized NotificationManager getInstance(Context context) {
        if (instance == null) {
            instance = new NotificationManager(context.getApplicationContext());
        }
        return instance;
    }

    public LiveData<Integer> getUnreadCount() {
        return unreadCountLiveData;
    }

    private void subscribeToNotifications() {
        wsService.subscribeToNotifications(currentUserId, message -> {
            GetNotificationResponse notification = gson.fromJson(message, GetNotificationResponse.class);
            notifications.add(0, notification);
            notificationsLiveData.postValue(notifications);
            updateUnreadCount(1);
        });

        wsService.subscribeToUnreadNotificationCount(currentUserId, message -> {
            long count = gson.fromJson(message, long.class);
            unreadCountLiveData.postValue((int) count);
        });
    }

    private void unSubscribeToNotifications()
    {
        wsService.unsubscribeFromUnreadCount();
        wsService.unsubscribeFromNotifications();
    }

    private void loadInitialNotifications() {
        notificationService.getUnreadNotifications(currentUserId).enqueue(new Callback<List<GetNotificationResponse>>() {
            @Override
            public void onResponse(Call<List<GetNotificationResponse>> call, Response<List<GetNotificationResponse>> response) {
                if (response.body() != null) {
                    notifications = response.body();
                    notificationsLiveData.postValue(notifications);
                }
            }

            @Override
            public void onFailure(Call<List<GetNotificationResponse>> call, Throwable t) { }
        });
    }

    private void loadNotificationCount() {
        notificationService.getNotificationCount(currentUserId).enqueue(new Callback<GetNotificationCountResponse>() {
            @Override
            public void onResponse(Call<GetNotificationCountResponse> call, Response<GetNotificationCountResponse> response) {
                if(response.body()!=null) {
                    unreadCountLiveData.postValue((int) response.body().getUnreadCount());
                }
            }

            @Override
            public void onFailure(Call<GetNotificationCountResponse> call, Throwable t) {}
        });
    }

    private void updateUnreadCount(long unreadCount) {
        unreadCountLiveData.postValue((int) unreadCount);
    }

    public void popNotification(GetNotificationResponse notification) {
        // Create a new list to avoid concurrent modification issues
        List<GetNotificationResponse> updatedNotifications = new ArrayList<>(notifications);
        updatedNotifications.remove(notification);

        // Update both the internal list and LiveData
        notifications = updatedNotifications;
        notificationsLiveData.postValue(notifications);

        // Update unread count
        Integer currentCount = unreadCountLiveData.getValue();
        if (currentCount != null && currentCount > 0) {
            unreadCountLiveData.postValue(currentCount - 1);
        }
    }

    public void popAllNotifications() {
        // Clear the list and post an empty list
        notifications = new ArrayList<>();
        notificationsLiveData.postValue(notifications);
        unreadCountLiveData.postValue(0);
    }

    public void loggedOff() {
        popAllNotifications();
        unSubscribeToNotifications();
        instance = null;
    }
}