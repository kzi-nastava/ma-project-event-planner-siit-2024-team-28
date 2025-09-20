package com.eventplanner.model.requests.chatMessages;

public class UpdateChatMessageAsSeenRequest {
    private Long chatMessageId;
    private Long recipientId;

    public UpdateChatMessageAsSeenRequest(Long chatMessageId, Long recipientId) {
        this.chatMessageId = chatMessageId;
        this.recipientId = recipientId;
    }

    public void setChatMessageId(Long chatMessageId) {
        this.chatMessageId = chatMessageId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }
}
