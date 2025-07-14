package com.eventplanner.model.requests.chatMessages;

public class CreateChatMessageRequest {
    private String content;
    private Long senderId;
    private Long recipientId;
    private Long chatId;

    private CreateChatMessageRequest(Builder builder) {
        this.content = builder.content;
        this.senderId = builder.senderId;
        this.recipientId = builder.recipientId;
        this.chatId = builder.chatId;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public static class Builder {
        private String content;
        private Long senderId;
        private Long recipientId;
        private Long chatId;

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder senderId(Long senderId) {
            this.senderId = senderId;
            return this;
        }

        public Builder recipientId(Long recipientId) {
            this.recipientId = recipientId;
            return this;
        }

        public Builder chatId(Long chatId) {
            this.chatId = chatId;
            return this;
        }

        public CreateChatMessageRequest build() {
            return new CreateChatMessageRequest(this);
        }
    }
}
