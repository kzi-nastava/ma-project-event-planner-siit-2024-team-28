package com.eventplanner.fragments.chat;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.eventplanner.R;
import com.eventplanner.adapters.chatMessages.ChatMessageListAdapter;
import com.eventplanner.databinding.FragmentChatBinding;
import com.eventplanner.model.requests.chatMessages.CreateChatMessageRequest;
import com.eventplanner.model.responses.ErrorResponse;
import com.eventplanner.model.responses.chatMessages.GetChatMessageResponse;
import com.eventplanner.model.responses.chats.GetChatResponse;
import com.eventplanner.model.responses.users.GetUserProfilePictureResponse;
import com.eventplanner.services.ChatMessageService;
import com.eventplanner.services.ChatService;
import com.eventplanner.services.UserService;
import com.eventplanner.utils.AuthUtils;
import com.eventplanner.utils.Base64Util;
import com.eventplanner.utils.HttpUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

import java.time.LocalDateTime;
import java.util.List;

import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import io.reactivex.disposables.Disposable;
import ua.naiksoftware.stomp.dto.LifecycleEvent;
import ua.naiksoftware.stomp.dto.StompMessage;
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
    private UserService userService;
    private StompClient stompClient;
    private Disposable stompConnection;
    private Disposable chatSubscription;
    private Gson gson;

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
        userService = HttpUtils.getUserService();
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>)
                        (json, type, context) -> LocalDateTime.parse(json.getAsString()))
                .create();
    }

    @Override
    public void onStart() {
        super.onStart();
        initStompConnection();
    }

    @Override
    public void onStop() {
        super.onStop();
        disconnectStomp();
    }


    private void initStompConnection() {
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, "ws://10.0.2.2:8080/ws-native");

        stompClient.connect();

        stompConnection = stompClient.lifecycle()
                .subscribe(lifecycleEvent -> {
                    LifecycleEvent.Type type = lifecycleEvent.getType();

                    switch (type) {
                        case OPENED:
                            Log.d("ChatFragment", "STOMP connection opened");
                            subscribeToChat();
                            break;
                        case ERROR:
                            Log.e("ChatFragment", "STOMP connection error", lifecycleEvent.getException());
                            break;
                        case CLOSED:
                            Log.d("ChatFragment", "STOMP connection closed");
                            break;
                    }
                }, throwable -> Log.e("ChatFragment", "STOMP lifecycle error", throwable));
    }


    private void subscribeToChat() {
        if (stompClient != null) {
            chatSubscription = stompClient.topic("/topic/chat/" + chatId)
                    .subscribe(topicMessage -> {
                        String payload = topicMessage.getPayload();
                        GetChatMessageResponse message = gson.fromJson(payload, GetChatMessageResponse.class);

                        requireActivity().runOnUiThread(() -> addNewMessage(message));
                    }, throwable -> Log.e("ChatFragment", "STOMP topic subscription error", throwable));
        }
    }


    private void disconnectStomp() {
        if (chatSubscription != null && !chatSubscription.isDisposed()) {
            chatSubscription.dispose();
        }
        if (stompConnection != null && !stompConnection.isDisposed()) {
            stompConnection.dispose();
        }
        if (stompClient != null) {
            stompClient.disconnect();
        }
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
                        if(!AuthUtils.getUserId(getContext()).equals(chat.getParticipant1Id())) {
                            binding.userName.setText(chat.getParticipant1Name());
                            fetchAndSetParticipantImage(chat.getParticipant1Id());
                        }
                        else {
                            binding.userName.setText(chat.getParticipant2Name());
                            fetchAndSetParticipantImage(chat.getParticipant2Id());
                        }
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

    private void fetchAndSetParticipantImage(Long participantId) {
        Call<GetUserProfilePictureResponse> call = userService.getUserProfilePictureBase64(participantId);

        call.enqueue(new Callback<GetUserProfilePictureResponse>() {
            @Override
            public void onResponse(Call<GetUserProfilePictureResponse> call, Response<GetUserProfilePictureResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String base64Image = response.body().getProfilePictureBase64();
                    if (base64Image != null && !base64Image.isEmpty()) {
                        Bitmap bitmap = Base64Util.decodeBase64ToBitmap(base64Image);
                        binding.userProfilePicture.setImageBitmap(bitmap);
                    } else {
                        Glide.with(requireContext())
                                .load(Base64Util.DEFAULT_IMAGE_URI)
                                .into(binding.userProfilePicture);
                    }
                } else {
                    Log.e("BusinessOwnerDetailsFragment", "Response failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GetUserProfilePictureResponse> call, Throwable t) {
                Log.e("BusinessOwnerDetailsFragment", "Network failure: " + t.getMessage(), t);
            }
        });
    }

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

        String jsonMessage = new Gson().toJson(request);

        stompClient.send("/app/send/message", jsonMessage)
                .subscribe(
                        () -> {
                            requireActivity().runOnUiThread(() -> {
                                binding.messageInput.setText("");
                                Toast.makeText(getContext(), "Message sent", Toast.LENGTH_SHORT).show();
                            });
                        },
                        throwable -> {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(getContext(), "Unsuccessful sending message via STOMP", Toast.LENGTH_SHORT).show()
                            );
                            Log.e("ChatFragment", "Unsuccessful sending message via STOMP", throwable);
                        }
                );

//        Call<GetChatMessageResponse> call = chatMessageService.createChatMessage(request);
//        call.enqueue(new Callback<>() {
//            @Override
//            public void onResponse(Call<GetChatMessageResponse> call, Response<GetChatMessageResponse> response) {
//                if (response.isSuccessful()) {
//                    GetChatMessageResponse message = response.body();
//                    addNewMessage(message);
//                    binding.messageInput.setText("");
//                } else {
//                    try {
//                        String errorJson = response.errorBody().string();
//                        ErrorResponse errorResponse = new Gson().fromJson(errorJson, ErrorResponse.class);
//                        Toast.makeText(getContext(), errorResponse.getError(), Toast.LENGTH_SHORT).show();
//                        Log.e("ChatFragment", "Send failed: " + errorResponse.getError());
//                    } catch (Exception e) {
//                        Toast.makeText(getContext(), "Send failed: Unknown error" + response.code(), Toast.LENGTH_SHORT).show();
//                        Log.e("ChatFragment", "Send failed: " + response.code());
//                    }
//                }
//            }
//
//            @Override
//            public void onFailure(Call<GetChatMessageResponse> call, Throwable t) {
//                Log.e("ChatFragment", "Network failure: " + t.getMessage());
//                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
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