package com.eventplanner.model.responses.notifications;

public class GetNotificationCountResponse {
    long unreadCount;
    long totalCount;

    public long getTotalCount() {
        return totalCount;
    }

    public long getUnreadCount() {
        return unreadCount;
    }
}
