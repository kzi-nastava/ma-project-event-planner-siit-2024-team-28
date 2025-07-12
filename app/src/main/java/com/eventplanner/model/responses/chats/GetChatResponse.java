package com.eventplanner.model.responses.chats;

import com.eventplanner.model.enums.ChatTheme;

public class GetChatResponse {
    private Long id;
    private Long participant1Id;
    private String participant1Name;
    private Long participant2Id;
    private String participant2Name;
    private ChatTheme theme;
    private Long themeId;
    private String themeName;

    public String getParticipant1Name() {
        return participant1Name;
    }

    public String getParticipant2Name() {
        return participant2Name;
    }

    public String getThemeName() {
        return themeName;
    }

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
