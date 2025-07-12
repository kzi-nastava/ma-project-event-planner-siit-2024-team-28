package com.eventplanner.services;

import com.eventplanner.model.responses.chatMessages.GetChatMessageResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ChatMessageService {
    @GET("chat-messages/chat/{chatId}/messages")
    Call<List<GetChatMessageResponse>> getMessagesByChatId(@Path("chatId") Long chatId);
}
