package com.eventplanner.services;

import android.util.Log;

import com.eventplanner.utils.WebSocketService;

import java.util.function.Consumer;

import io.reactivex.disposables.Disposable;
import ua.naiksoftware.stomp.StompClient;

public class NotificationWebSocketService {
    private final StompClient stompClient;

    private Disposable notificationsSubscription;
    private Disposable unreadCountSubscription;

    public NotificationWebSocketService() {
        // Directly takes singleton stomp-client
        this.stompClient = WebSocketService.getInstance().getClient();
    }

    public void subscribeToNotifications(Long userId, Consumer<String> onMessage) {
        //while (!stompClient.isConnected()){}
        if (stompClient != null && stompClient.isConnected()) {
            notificationsSubscription = stompClient.topic("/topic/notifications/user/" + userId)
                    .subscribe(topicMessage -> onMessage.accept(topicMessage.getPayload()),
                            throwable -> Log.e("NotificationsWebSocketService", "STOMP topic notifications error", throwable));
        }
    }

    public void subscribeToUnreadNotificationCount(Long userId, Consumer<String> onMessage) {
        //while (!stompClient.isConnected()) {}
        if (stompClient != null && stompClient.isConnected()) {
            unreadCountSubscription = stompClient.topic("/topic/notifications/user/" + userId + "/count")
                    .subscribe(topicMessage -> onMessage.accept(topicMessage.getPayload()),
                            throwable -> Log.e("NotificationsWebSocketService", "Failed subscribing to unread count", throwable));
        }
    }

    public void unsubscribeFromNotifications() {
        if (notificationsSubscription != null && !notificationsSubscription.isDisposed()) {
            notificationsSubscription.dispose();
        }
    }

    public void unsubscribeFromUnreadCount() {
        if (unreadCountSubscription != null && !unreadCountSubscription.isDisposed()) {
            unreadCountSubscription.dispose();
        }
    }
}
