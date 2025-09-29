package com.eventplanner.model.responses.chatMessages;

import java.time.LocalDateTime;

public class GetChatMessageResponse {
    private Long id;
    private String content;
    private LocalDateTime timestamp;
    private Long senderId;
    private Long recipientId;

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Long getSenderId() {
        return senderId;
    }

    public Long getRecipientId() {
        return recipientId;
    }
}
