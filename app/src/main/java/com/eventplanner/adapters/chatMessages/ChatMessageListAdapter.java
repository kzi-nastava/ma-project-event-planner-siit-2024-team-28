package com.eventplanner.adapters.chatMessages;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.eventplanner.R;
import com.eventplanner.model.responses.chatMessages.GetChatMessageResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ChatMessageListAdapter extends ArrayAdapter<GetChatMessageResponse> {
    private Long userId;

    public ChatMessageListAdapter(Context context, List<GetChatMessageResponse> messages, Long userId) {
        super(context, 0, messages);
        this.userId = userId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.chat_message_card, parent, false);
        }

        GetChatMessageResponse message = getItem(position);

        LinearLayout messageContainer = convertView.findViewById(R.id.messageContainer);
        TextView messageText = convertView.findViewById(R.id.messageText);
        TextView messageTime = convertView.findViewById(R.id.messageTime);

        // Setting up the view
        if (message != null) {
            // Bubble message modification in relation to sender
            if (message.getSenderId().equals(userId)) {
                messageContainer.setBackgroundResource(R.drawable.bg_chat_bubble_user);
                messageContainer.setGravity(Gravity.END);
            } else {
                messageContainer.setBackgroundResource(R.drawable.bg_chat_bubble_other);
                messageContainer.setGravity(Gravity.START);
            }
            // Message and timestamp
            if (message.getContent() != null)
                messageText.setText(message.getContent());
            if (message.getTimestamp() != null)
                messageTime.setText(formatMessageTime(message.getTimestamp()));
        }

        return convertView;
    }

    private String formatMessageTime(LocalDateTime timestamp) {
        if (timestamp == null) {
            return "";
        }

        LocalDate today = LocalDate.now();
        LocalDate messageDate = timestamp.toLocalDate();

        DateTimeFormatter formatter;
        if (messageDate.isEqual(today)) {
            formatter = DateTimeFormatter.ofPattern("HH:mm");
        } else if (messageDate.isEqual(today.minusDays(1))) {
            return "Yesterday " + timestamp.format(DateTimeFormatter.ofPattern("HH:mm"));
        } else if (messageDate.isAfter(today.minusDays(7))) {
            return timestamp.format(DateTimeFormatter.ofPattern("EEEE HH:mm"));
        } else {
            formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        }

        return timestamp.format(formatter);
    }
}
