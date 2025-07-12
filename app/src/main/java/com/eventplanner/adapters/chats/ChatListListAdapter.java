package com.eventplanner.adapters.chats;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.eventplanner.R;
import com.eventplanner.model.enums.ChatTheme;
import com.eventplanner.model.responses.chats.GetChatListResponse;

import java.util.List;

public class ChatListListAdapter extends ArrayAdapter<GetChatListResponse> {
    private OnClickListener listener;

    public ChatListListAdapter(Context context, List<GetChatListResponse> chatList, OnClickListener listener) {
        super(context, 0, chatList);
        this.listener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.chat_card, parent, false);
        }

        GetChatListResponse chat = getItem(position);

        TextView chatterName = convertView.findViewById(R.id.chatterName);
        TextView chatSubject = convertView.findViewById(R.id.chatSubject);
        TextView lastMessage = convertView.findViewById(R.id.lastMessage);
        // TODO: srediti slike
        ImageView chatterImage = convertView.findViewById(R.id.chatterImage);

        chatterName.setText(chat.participantName());
        if(chat.theme().equals(ChatTheme.SOLUTION))
            chatSubject.setText(chat.solutionName());
        else if(chat.theme().equals(ChatTheme.EVENT))
            chatSubject.setText(chat.eventName());
        lastMessage.setText(chat.lastMessage() != null ? chat.lastMessage() : "No messages");

        // Setting on click listener
        convertView.setOnClickListener(v -> {
            if (listener != null && chat != null)
                listener.onChatClickListener(chat);
        });

        return convertView;
    }

    // Listener user for resolving onClick event in fragments
    public interface OnClickListener {
        void onChatClickListener(GetChatListResponse chat);
    }
}
