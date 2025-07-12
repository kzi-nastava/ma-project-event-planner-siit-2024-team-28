package com.eventplanner.services;

import com.eventplanner.model.responses.chats.GetChatListResponse;

import java.util.Collection;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ChatService {
    @GET("chats/chat-list/{userId}")
    Call<Collection<GetChatListResponse>> getUsersChatList(@Path("userId") Long userId);
}
