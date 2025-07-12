package com.eventplanner.fragments.chat;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.eventplanner.R;
import com.eventplanner.adapters.chatMessages.ChatMessageListAdapter;
import com.eventplanner.databinding.FragmentChatBinding;
import com.eventplanner.model.requests.chatMessages.CreateChatMessageRequest;
import com.eventplanner.model.responses.ErrorResponse;
import com.eventplanner.model.responses.chatMessages.GetChatMessageResponse;
import com.eventplanner.model.responses.chats.GetChatResponse;
import com.eventplanner.services.ChatMessageService;
import com.eventplanner.services.ChatService;
import com.eventplanner.utils.AuthUtils;
import com.eventplanner.utils.HttpUtils;
import com.google.gson.Gson;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatFragment extends Fragment {
    private FragmentChatBinding binding;
    private static final String ARG_CHAT_ID = "chatId";
    private static Long chatId;
    private GetChatResponse chat;
    private ChatMessageListAdapter messageListAdapter;
    private ChatService chatService;
    private ChatMessageService chatMessageService;

    public ChatFragment() {
        // Required empty public constructor
    }

    public static ChatFragment newInstance() {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_CHAT_ID, chatId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            chatId = getArguments().getLong(ARG_CHAT_ID);
        }
        chatService = HttpUtils.getChatService();
        chatMessageService = HttpUtils.getChatMessageService();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentChatBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        loadChatDetails();
        loadMessages();

        binding.sendButton.setOnClickListener(v -> {
            if(!binding.messageInput.getText().equals(""))
                sendMessage();
        });

        return view;
    }

    private void loadChatDetails() {
        Call<GetChatResponse> call = chatService.getChat(chatId);

        call.enqueue(new Callback<GetChatResponse>() {
            @Override
            public void onResponse(Call<GetChatResponse> call, Response<GetChatResponse> response) {
                if (response.isSuccessful()) {
                    chat = response.body();
                    if (chat != null) {
                        // TODO: srediti sliku
                        if(!AuthUtils.getUserId(getContext()).equals(chat.getParticipant1Id()))
                            binding.userName.setText(chat.getParticipant1Name());
                        else
                            binding.userName.setText(chat.getParticipant2Name());
                        binding.themeName.setText(getString(R.string.theme_for) + " " + chat.getThemeName());
                    }
                } else {
                    try {
                        String errorJson = response.errorBody().string();
                        ErrorResponse errorResponse = new Gson().fromJson(errorJson, ErrorResponse.class);
                        Toast.makeText(getContext(), errorResponse.getError(), Toast.LENGTH_SHORT).show();
                        Log.e("ChatFragment", "Failed to fetch chat: " + errorResponse.getError());
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Failed to fetch chat", Toast.LENGTH_SHORT).show();
                        Log.e("ChatFragment", "Failed to fetch chat: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(Call<GetChatResponse> call, Throwable t) {
                Log.e("ChatFragment", "Network failure: " + t.getMessage());
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void loadMessages() {
        Call<List<GetChatMessageResponse>> call = chatMessageService.getMessagesByChatId(chatId);

        call.enqueue(new Callback<List<GetChatMessageResponse>>() {
            @Override
            public void onResponse(Call<List<GetChatMessageResponse>> call, Response<List<GetChatMessageResponse>> response) {
                if (response.isSuccessful()) {
                    List<GetChatMessageResponse> messages = response.body();
                    setChatMessagesAdapter(messages);
                } else {
                    try {
                        String errorJson = response.errorBody().string();
                        ErrorResponse errorResponse = new Gson().fromJson(errorJson, ErrorResponse.class);
                        Toast.makeText(getContext(), errorResponse.getError(), Toast.LENGTH_SHORT).show();
                        Log.e("ChatFragment", "Failed to get messages: " + errorResponse.getError());
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Failed to get messages", Toast.LENGTH_SHORT).show();
                        Log.e("ChatFragment", "Failed to get messages: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(Call<List<GetChatMessageResponse>> call, Throwable t) {
                Log.e("ChatFragment", "Network failure: " + t.getMessage());
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    };

    private void setChatMessagesAdapter(List<GetChatMessageResponse> messages) {
        messageListAdapter = new ChatMessageListAdapter(getContext(), messages, AuthUtils.getUserId(getContext()));
        binding.messagesListView.setAdapter(messageListAdapter);
        scrollToTop();
    }

    private void sendMessage() {
        Long recipientId = chat.getParticipant1Id().equals(AuthUtils.getUserId(getContext())) ? chat.getParticipant2Id() : chat.getParticipant1Id();
        String content = binding.messageInput.getText().toString().trim();

        CreateChatMessageRequest request = new CreateChatMessageRequest.Builder()
                .content(content)
                .senderId(AuthUtils.getUserId(getContext()))
                .recipientId(recipientId)
                .chatId(chatId)
                .build();

        Call<GetChatMessageResponse> call = chatMessageService.createChatMessage(request);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<GetChatMessageResponse> call, Response<GetChatMessageResponse> response) {
                if (response.isSuccessful()) {
                    GetChatMessageResponse message = response.body();
                    addNewMessage(message);
                    binding.messageInput.setText("");
                } else {
                    try {
                        String errorJson = response.errorBody().string();
                        ErrorResponse errorResponse = new Gson().fromJson(errorJson, ErrorResponse.class);
                        Toast.makeText(getContext(), errorResponse.getError(), Toast.LENGTH_SHORT).show();
                        Log.e("ChatFragment", "Send failed: " + errorResponse.getError());
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Send failed: Unknown error" + response.code(), Toast.LENGTH_SHORT).show();
                        Log.e("ChatFragment", "Send failed: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(Call<GetChatMessageResponse> call, Throwable t) {
                Log.e("ChatFragment", "Network failure: " + t.getMessage());
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void addNewMessage(GetChatMessageResponse message) {
        if (messageListAdapter != null) {
            messageListAdapter.addNewMessage(message);
            scrollToTop();
        }
    }

    private void scrollToTop() {
        binding.messagesListView.post(() -> {
            binding.messagesListView.setSelection(messageListAdapter.getCount() - 1);
        });
    }
}