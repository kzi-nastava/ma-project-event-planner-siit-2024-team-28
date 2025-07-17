package com.eventplanner.adapters.eventTypes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eventplanner.R;
import com.eventplanner.model.responses.eventTypes.GetEventTypeResponse;

import java.util.List;

public class EventTypesAdapter extends RecyclerView.Adapter<EventTypesAdapter.ViewHolder> {
    public interface ActionCallback {
        void onActivateToggle(GetEventTypeResponse type);

        void onEdit(GetEventTypeResponse type);
    }

    private final List<GetEventTypeResponse> eventTypes;
    private final ActionCallback callback;

    public EventTypesAdapter(List<GetEventTypeResponse> eventTypes, ActionCallback callback) {
        this.eventTypes = eventTypes;
        this.callback = callback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event_type, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GetEventTypeResponse eventType = eventTypes.get(position);
        holder.bind(eventType, callback);
    }

    @Override
    public int getItemCount() {
        return eventTypes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final TextView description;
        private final TextView status;
        private final Button btnActivate;
        private final Button btnEdit;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textName);
            description = itemView.findViewById(R.id.textDescription);
            status = itemView.findViewById(R.id.textStatus);
            btnActivate = itemView.findViewById(R.id.btnActivate);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }

        public void bind(GetEventTypeResponse type, ActionCallback callback) {
            name.setText(type.getName());
            description.setText(type.getDescription());
            status.setText(type.getIsActive() ? "Active" : "Inactive");

            btnActivate.setText(type.getIsActive() ? "Deactivate" : "Activate");
            btnActivate.setOnClickListener(v -> callback.onActivateToggle(type));
            btnEdit.setOnClickListener(v -> callback.onEdit(type));
        }
    }
}
