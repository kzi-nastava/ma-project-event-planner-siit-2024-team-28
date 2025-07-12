package com.eventplanner.services;

import com.eventplanner.model.requests.chats.CreateChatRequest;
import com.eventplanner.model.requests.chats.FindChatRequest;
import com.eventplanner.model.responses.chats.FindChatResponse;
import com.eventplanner.model.responses.chats.GetChatListResponse;

import java.util.Collection;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ChatService {
    @POST("chats")
    Call<Long> createChat(@Body CreateChatRequest request);

    @GET("chats/chat-list/{userId}")
    Call<Collection<GetChatListResponse>> getUsersChatList(@Path("userId") Long userId);

    @POST("chats/participants-theme")
    Call<FindChatResponse> getChatByParticipantsAndTheme(@Body FindChatRequest request);
}
