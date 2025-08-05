package com.eventplanner.adapters.chats;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.eventplanner.R;
import com.eventplanner.model.enums.ChatTheme;
import com.eventplanner.model.responses.chats.GetChatListResponse;
import com.eventplanner.utils.Base64Util;

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
        ImageView chatterImage = convertView.findViewById(R.id.chatterImage);
        if (chat.participantImage() == null) {
            Glide.with(getContext())
                    .load(Base64Util.DEFAULT_IMAGE_URI)
                    .into(chatterImage);
        } else {
            Bitmap bitmap = Base64Util.decodeBase64ToBitmap(chat.participantImage());
            chatterImage.setImageBitmap(bitmap);
        }

        chatterName.setText(chat.participantName());
        chatSubject.setText(chat.themeName());
        lastMessage.setText(chat.lastMessage() != null ? "\"" + chat.lastMessage() + "\"" : "No messages");

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
