package com.eventplanner.model.responses.chats;

import com.eventplanner.model.enums.ChatTheme;

public class GetChatResponse {
    private Long id;
    private Long participant1Id;
    private Long participant2Id;
    private ChatTheme theme;
    private Long themeId;

    public Long getId() {
        return id;
    }

    public Long getParticipant1Id() {
        return participant1Id;
    }

    public Long getParticipant2Id() {
        return participant2Id;
    }

    public ChatTheme getTheme() {
        return theme;
    }

    public Long getThemeId() {
        return themeId;
    }
}
