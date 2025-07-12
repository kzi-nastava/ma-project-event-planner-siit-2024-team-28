package com.eventplanner.fragments.chat;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.eventplanner.R;
import com.eventplanner.adapters.chats.ChatListListAdapter;
import com.eventplanner.databinding.FragmentChatListBinding;
import com.eventplanner.model.responses.ErrorResponse;
import com.eventplanner.model.responses.chats.GetChatListResponse;
import com.eventplanner.services.ChatService;
import com.eventplanner.utils.AuthUtils;
import com.eventplanner.utils.HttpUtils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatListFragment extends Fragment {
    FragmentChatListBinding binding;
    ChatService chatService;
    NavController navController;


    public ChatListFragment() {
        // Required empty public constructor
    }

    public static ChatListFragment newInstance() {
        ChatListFragment fragment = new ChatListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatService = HttpUtils.getChatService();
        navController = Navigation.findNavController(getActivity(), R.id.fragment_nav_content_main);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentChatListBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        fetchChats();

        return view;
    }

    private void fetchChats() {
        Long userId = AuthUtils.getUserId(getContext());
        Call<Collection<GetChatListResponse>> call = chatService.getUsersChatList(userId);

        call.enqueue(new Callback<Collection<GetChatListResponse>>() {
            @Override
            public void onResponse(Call<Collection<GetChatListResponse>> call, Response<Collection<GetChatListResponse>> response) {
                if (response.isSuccessful()) {
                    Collection<GetChatListResponse> chatList = response.body();
                    if(chatList != null && !chatList.isEmpty()) {
                        setChatListListAdapter(new ArrayList<>(chatList));
                        binding.chatListListView.setVisibility(View.VISIBLE);
                        binding.emptyChatListText.setVisibility(View.GONE);
                    } else {
                        binding.chatListListView.setVisibility(View.GONE);
                        binding.emptyChatListText.setVisibility(View.VISIBLE);
                    }
                } else {
                    try {
                        String errorJson = response.errorBody().string();
                        Gson gson = new Gson();
                        ErrorResponse errorResponse = gson.fromJson(errorJson, ErrorResponse.class);
                        Toast.makeText(getContext(), errorResponse.getError(), Toast.LENGTH_SHORT).show();
                        Log.e("ChatListFragment", "Failed to get chat list: " + errorResponse.getError());
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Failed to get chat list", Toast.LENGTH_SHORT).show();
                        Log.e("ChatListFragment", "Failed to get chat list: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(Call<Collection<GetChatListResponse>> call, Throwable t) {
                Log.e("ChatListFragment", "Network failure: " + t.getMessage());
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setChatListListAdapter(List<GetChatListResponse> chatList) {
        ChatListListAdapter.OnClickListener listener = chat -> {
            Bundle bundle = new Bundle();
            bundle.putLong("chatId", chat.chatId());

            navController
                    .navigate(R.id.action_chat_list_to_chat, bundle);
        };

        ChatListListAdapter adapter = new ChatListListAdapter(getContext(), chatList, listener);
        binding.chatListListView.setAdapter(adapter);
    }
}