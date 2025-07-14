package com.eventplanner.model.responses.chats;

import com.eventplanner.model.enums.ChatTheme;

public class GetChatListResponse {
    private Long chatId;
    private ChatTheme theme;
    private String participantName;
    private String participantImage;
    private String themeName;
    private String lastMessage;

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
}
