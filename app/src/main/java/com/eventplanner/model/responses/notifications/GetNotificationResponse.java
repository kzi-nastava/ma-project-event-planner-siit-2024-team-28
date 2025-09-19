package com.eventplanner.model.responses.notifications;

import com.eventplanner.model.enums.NotificationType;

import java.time.LocalDateTime;

public class GetNotificationResponse {
    Long id;
    NotificationType type;
    String title;
    String description;
    Boolean isRead;
    LocalDateTime createdAt;
    String senderName;
    String relatedEntityId;
    String actionUrl;

    public Long getId() {
        return id;
    }

    public NotificationType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getRelatedEntityId() {
        return relatedEntityId;
    }

    public void setIsRead(boolean b) {
        isRead = b;
    }
}
