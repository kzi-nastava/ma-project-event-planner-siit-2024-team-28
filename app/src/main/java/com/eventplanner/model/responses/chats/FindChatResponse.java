package com.eventplanner.model.responses.chats;

public class FindChatResponse {
    private GetChatResponse chat;
    private boolean found;

    public GetChatResponse getChat() {
        return chat;
    }

    public boolean isFound() {
        return found;
    }
}
