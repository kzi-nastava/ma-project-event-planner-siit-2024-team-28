package com.eventplanner.adapters.notifications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.eventplanner.R;
import com.eventplanner.model.responses.notifications.GetNotificationResponse;
import com.eventplanner.model.responses.notifications.GetNotificationCountResponse;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.VH> {

    private List<GetNotificationResponse> items = new ArrayList<>();
    private OnItemClickListener listener;

    public void setNotifications(List<GetNotificationResponse> notifications) {
        this.items = notifications;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(GetNotificationResponse notification);
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        this.listener = l;
    }


    public void addItemAtTop(GetNotificationResponse n) {
        items.add(0, n);
        if (items.size() > 5) { // same behaviour: keep 5
            items.remove(items.size() - 1);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        GetNotificationResponse n = items.get(position);
        holder.title.setText(n.getTitle() != null && !n.getTitle().isEmpty() ? n.getTitle() : "No title");
        holder.desc.setText(n.getDescription() != null && !n.getDescription().isEmpty() ? n.getDescription() : "No description available");
        Duration duration = Duration.between(n.getCreatedAt(), LocalDateTime.now());
        holder.time.setText(formatTimeAgo(duration));
        holder.sender.setText(n.getSenderName() != null ? "From: " + n.getSenderName() : "");
        holder.type.setText(n.getType() != null ? n.getType().toString() : "UNKNOWN");
        holder.unreadIndicator.setVisibility(!n.getIsRead() ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(n);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, desc, time, sender, type;
        View unreadIndicator;
        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.notification_title);
            desc = itemView.findViewById(R.id.notification_description);
            time = itemView.findViewById(R.id.notification_time);
            sender = itemView.findViewById(R.id.notification_sender);
            type = itemView.findViewById(R.id.notification_type);
            unreadIndicator = itemView.findViewById(R.id.unread_indicator);
        }
    }
    private String formatTimeAgo(Duration duration) {
        long seconds = Math.abs(duration.getSeconds());

        if (seconds < 60) return "now";
        if (seconds < 3600) return (seconds / 60) + "m";
        if (seconds < 86400) return (seconds / 3600) + "h";
        if (seconds < 2592000) return (seconds / 86400) + "d";
        if (seconds < 31536000) return (seconds / 2592000) + "mo";
        return (seconds / 31536000) + "y";
    }
}
