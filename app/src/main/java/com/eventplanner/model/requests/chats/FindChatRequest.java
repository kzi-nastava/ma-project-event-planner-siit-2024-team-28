package com.eventplanner.model.requests.chats;

import com.eventplanner.model.enums.ChatTheme;

public class FindChatRequest {
    private Long participant1Id;
    private Long participant2Id;
    private ChatTheme theme;
    private Long themeId;

    public FindChatRequest(Long participant1Id, Long participant2Id, ChatTheme theme, Long themeId) {
        this.participant1Id = participant1Id;
        this.participant2Id = participant2Id;
        this.theme = theme;
        this.themeId = themeId;
    }
}
