package com.eventplanner.model.responses.chats;

import com.eventplanner.model.enums.ChatTheme;

public class GetChatListResponse {
    private Long chatId;
    private ChatTheme theme;
    private String participantName;
    private String participantImage;
    private String solutionName;
    private String eventName;
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

    public String solutionName() {
        return solutionName;
    }

    public String eventName() {
        return eventName;
    }

    public String lastMessage() {
        return lastMessage;
    }
}
