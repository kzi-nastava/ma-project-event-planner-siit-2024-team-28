package com.eventplanner.adapters.events;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.eventplanner.R;
import com.eventplanner.adapters.products.ProductImageAdapter;
import com.eventplanner.model.responses.events.GetEventResponse;
import com.eventplanner.utils.AuthUtils;
import com.eventplanner.utils.Base64Util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AllEventsAdapter extends RecyclerView.Adapter<AllEventsAdapter.EventViewHolder> {

    public interface OnEventClickListener {
        void onEventClick(GetEventResponse event);
    }

    private Context context;
    private List<GetEventResponse> events;
    private OnEventClickListener listener;

    public AllEventsAdapter(Context context, List<GetEventResponse> events) {
        this.context = context;
        this.events = events;
    }

    public void setOnEventClickListener(OnEventClickListener listener) {
        this.listener = listener;
    }

    public void updateEvents(List<GetEventResponse> newEvents) {
        this.events = newEvents;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        GetEventResponse event = events.get(position);

        holder.nameTextView.setText(event.getName());
        holder.descriptionTextView.setText(event.getDescription());
        holder.dateTextView.setText(event.getStartDate());

        // Show event type if available
        if (event.getEventTypeName() != null && !event.getEventTypeName().isEmpty()) {
            holder.typeTextView.setText(event.getEventTypeName());
            holder.typeTextView.setVisibility(View.VISIBLE);
        } else {
            holder.typeTextView.setVisibility(View.GONE);
        }


        String base64Image = event.getImageBase64();
        if (base64Image != null && !base64Image.isEmpty()) {
            Bitmap bitmap = Base64Util.decodeBase64ToBitmap(base64Image);
            if (bitmap != null) {
                holder.imageView.setImageBitmap(bitmap);
            } else {
                holder.imageView.setImageResource(R.drawable.ic_image_placeholder);
            }
        } else {
            holder.imageView.setImageResource(R.drawable.ic_image_placeholder);
        }


        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEventClick(event);
            }
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, descriptionTextView, dateTextView, typeTextView;
        ImageView imageView;
        Button editButton, deleteButton;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.event_name);
            descriptionTextView = itemView.findViewById(R.id.event_description);
            dateTextView = itemView.findViewById(R.id.event_date);
            typeTextView = itemView.findViewById(R.id.event_type);
            imageView = itemView.findViewById(R.id.event_image_view);
            editButton = itemView.findViewById(R.id.button_edit);
            deleteButton = itemView.findViewById(R.id.button_delete);
        }
    }
}
