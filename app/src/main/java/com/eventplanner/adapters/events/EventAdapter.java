package com.eventplanner.adapters.events;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eventplanner.R;
import com.eventplanner.model.responses.events.GetEventResponse;
import com.eventplanner.model.responses.products.GetProductResponse;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private List<GetEventResponse> events;
    private OnEventClickListener listener;

    public interface OnEventClickListener {
        void onEventClick(GetEventResponse event);
    }

    public EventAdapter(List<GetEventResponse> events, OnEventClickListener listener) {
        this.events = events;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        GetEventResponse event = events.get(position);

        holder.nameTextView.setText(event.getName());
        holder.descriptionTextView.setText(event.getDescription());
        holder.dateTextView.setText(event.getStartDate());

        // For now, just show the ViewPager without adapter
        if (event.getImageBase64() != null) {
            // TODO: Implement proper image display when ImageCarouselAdapter is available
            holder.imageViewPager.setVisibility(View.VISIBLE);
        } else {
            holder.imageViewPager.setVisibility(View.VISIBLE);
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
        TextView nameTextView, descriptionTextView, dateTextView;
        View imageViewPager;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.event_name);
            descriptionTextView = itemView.findViewById(R.id.event_description);
            imageViewPager = itemView.findViewById(R.id.image_view_pager);
            dateTextView = itemView.findViewById(R.id.event_date);
        }
    }
}