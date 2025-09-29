package com.eventplanner.adapters.chats;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.eventplanner.R;
import com.eventplanner.model.responses.chatMessages.GetNewChatMessageResponse;
import com.eventplanner.model.responses.chats.GetChatListResponse;
import com.eventplanner.utils.Base64Util;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatListListAdapter extends ArrayAdapter<GetChatListResponse> {
    private OnClickListener listener;
    private Map<Long, View> chatViews = new HashMap<>();

    public ChatListListAdapter(Context context, List<GetChatListResponse> chatList, OnClickListener listener) {
        super(context, 0, chatList);
        this.listener = listener;
        sortChats();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.chat_card, parent, false);
        }

        GetChatListResponse chat = getItem(position);

        if (chat != null) chatViews.put(chat.chatId(), convertView);

        TextView chatterName = convertView.findViewById(R.id.chatterName);
        TextView chatSubject = convertView.findViewById(R.id.chatSubject);
        TextView lastMessage = convertView.findViewById(R.id.lastMessage);
        TextView newMessageIdentificator = convertView.findViewById(R.id.newMessage);
        ImageView chatterImage = convertView.findViewById(R.id.chatterImage);
        LinearLayout rootLayout = (LinearLayout) convertView;
        if (chat.participantImage() == null) {
            Glide.with(getContext())
                    .load(Base64Util.DEFAULT_IMAGE_URI)
                    .into(chatterImage);
        } else {
            Bitmap bitmap = Base64Util.decodeBase64ToBitmap(chat.participantImage());
            chatterImage.setImageBitmap(bitmap);
        }

        rootLayout.setBackgroundColor(getContext().getColor(R.color.light_black));
        newMessageIdentificator.setVisibility(View.GONE);
        chatterName.setText(chat.participantName());
        chatSubject.setText(chat.themeName());
        lastMessage.setText(chat.lastMessage() != null ? "\"" + chat.lastMessage() + "\"" : "No messages");

        if(chat.getHasUnreadMessages()) {
            rootLayout.setBackgroundColor(getContext().getColor(R.color.cool_dark_purple));
        }

        // Setting on click listener
        convertView.setOnClickListener(v -> {
            if (listener != null && chat != null)
                listener.onChatClickListener(chat);
        });

        return convertView;
    }

    public void setNewMessage(GetNewChatMessageResponse newMessage) {
        View view = chatViews.get(newMessage.getChatId());
        if (view != null) {
            TextView lastMessage = view.findViewById(R.id.lastMessage);
            TextView newMessageIdentificator = view.findViewById(R.id.newMessage);
            LinearLayout rootLayout = (LinearLayout) view;

            lastMessage.setText("\"" + newMessage.getNewMessage() + "\"");
            newMessageIdentificator.setVisibility(View.VISIBLE);
            rootLayout.setBackgroundColor(getContext().getColor(R.color.cool_dark_purple));
        }
    }

    public void removeNewMessage(Long chatId) {
        View view = chatViews.get(chatId);
        if (view != null) {
            TextView newMessageIdentificator = view.findViewById(R.id.newMessage);
            LinearLayout rootLayout = (LinearLayout) view;
            newMessageIdentificator.setVisibility(View.GONE);
            rootLayout.setBackgroundColor(getContext().getColor(R.color.light_black));
        }
    }

    // Listener user for resolving onClick event in fragments
    public interface OnClickListener {
        void onChatClickListener(GetChatListResponse chat);
    }

    private void sortChats() {
        sort((c1, c2) -> {
            LocalDateTime t1 = c1.getLastMessageTimeStamp();
            LocalDateTime t2 = c2.getLastMessageTimeStamp();

            if (t1 == null && t2 == null) return 0;
            if (t1 == null) return 1;
            if (t2 == null) return -1;

            return t2.compareTo(t1);
        });
    }
}
