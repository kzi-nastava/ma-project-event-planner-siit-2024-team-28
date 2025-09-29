package com.eventplanner.model.responses.chatMessages;

public class GetNewChatMessageResponse {
    Long chatId;
    String newMessage;

    public Long getChatId() {
        return chatId;
    }

    public String getNewMessage() {
        return newMessage;
    }
}
