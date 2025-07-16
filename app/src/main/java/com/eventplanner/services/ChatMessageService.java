package com.eventplanner.services;

import com.eventplanner.model.requests.chatMessages.CreateChatMessageRequest;
import com.eventplanner.model.responses.chatMessages.GetChatMessageResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ChatMessageService {
    @POST("chat-messages")
    Call<GetChatMessageResponse> createChatMessage(@Body CreateChatMessageRequest request);

    @GET("chat-messages/chat/{chatId}/messages")
    Call<List<GetChatMessageResponse>> getMessagesByChatId(@Path("chatId") Long chatId);
}
