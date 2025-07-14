package com.eventplanner.model.requests.chats;

import com.eventplanner.model.enums.ChatTheme;

public class CreateChatRequest {
    private Long participant1Id;
    private Long participant2Id;
    private ChatTheme theme;
    private Long themeId;

    public void setParticipant1Id(Long participant1Id) {
        this.participant1Id = participant1Id;
    }

    public void setParticipant2Id(Long participant2Id) {
        this.participant2Id = participant2Id;
    }

    public void setTheme(ChatTheme theme) {
        this.theme = theme;
    }

    public void setThemeId(Long themeId) {
        this.themeId = themeId;
    }

    private CreateChatRequest(Builder builder) {
        this.participant1Id = builder.participant1Id;
        this.participant2Id = builder.participant2Id;
        this.theme = builder.theme;
        this.themeId = builder.themeId;
    }

    public static class Builder {
        private Long participant1Id;
        private Long participant2Id;
        private ChatTheme theme;
        private Long themeId;

        public Builder participant1Id(Long participant1Id) {
            this.participant1Id = participant1Id;
            return this;
        }

        public Builder participant2Id(Long participant2Id) {
            this.participant2Id = participant2Id;
            return this;
        }

        public Builder theme(ChatTheme theme) {
            this.theme = theme;
            return this;
        }

        public Builder themeId(Long themeId) {
            this.themeId = themeId;
            return this;
        }

        public CreateChatRequest build() {
            return new CreateChatRequest(this);
        }
    }
}
