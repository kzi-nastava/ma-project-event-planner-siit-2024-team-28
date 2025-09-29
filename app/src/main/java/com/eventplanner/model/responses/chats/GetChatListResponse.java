package com.eventplanner.model.responses.chats;

import com.eventplanner.model.enums.ChatTheme;

import java.time.LocalDateTime;

public class GetChatListResponse {
    private Long chatId;
    private ChatTheme theme;
    private String participantName;
    private String participantImage;
    private String themeName;
    private String lastMessage;
    private LocalDateTime lastMessageTimeStamp;
    private Long unreadMessageCount;
    private Boolean hasUnreadMessages;

    public Long chatId() {
        return chatId;
    }

    public ChatTheme theme() {
        return theme;
    }

    public String participantName() {
        return participantName;
    }

    public String participantImage() {
        return participantImage;
    }

    public String themeName() {
        return themeName;
    }

    public String lastMessage() {
        return lastMessage;
    }

    public Boolean getHasUnreadMessages() {
        return hasUnreadMessages;
    }

    public Long getUnreadMessageCount() {
        return unreadMessageCount;
    }

    public LocalDateTime getLastMessageTimeStamp() {
        return lastMessageTimeStamp;
    }
}
