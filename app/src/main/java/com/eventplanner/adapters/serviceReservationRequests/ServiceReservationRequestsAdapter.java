package com.eventplanner.adapters.serviceReservationRequests;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eventplanner.R;
import com.eventplanner.model.responses.serviceReservationRequests.GetServiceReservationRequestResponse;

import java.util.ArrayList;
import java.util.List;

public class ServiceReservationRequestsAdapter extends RecyclerView.Adapter<ServiceReservationRequestsAdapter.ViewHolder> {

    public interface OnReservationActionListener {
        void onStatusChange(GetServiceReservationRequestResponse request, String newStatus);
    }

    private final List<GetServiceReservationRequestResponse> reservations = new ArrayList<>();
    private final OnReservationActionListener listener;
    private final Context context;
    private long editingId = -1;
    private String tempStatus = "";

    public ServiceReservationRequestsAdapter(Context context, OnReservationActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setReservations(List<GetServiceReservationRequestResponse> newReservations) {
        reservations.clear();
        reservations.addAll(newReservations);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_service_reservation_request, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GetServiceReservationRequestResponse r = reservations.get(position);

        holder.txtId.setText("#" + r.getId());
        holder.txtDate.setText(r.getDate());
        holder.txtStart.setText(r.getStartTime());
        holder.txtEnd.setText(r.getEndTime());
        holder.txtService.setText("Service #" + r.getServiceId());
        holder.txtEvent.setText("Event #" + r.getEventId());

        if (editingId == r.getId()) {
            holder.statusView.setVisibility(View.GONE);
            holder.statusEditContainer.setVisibility(View.VISIBLE);

            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                    context,
                    R.array.statuses,
                    android.R.layout.simple_spinner_item
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            holder.statusSpinner.setAdapter(adapter);

            int pos = adapter.getPosition(r.getStatus().toString());
            holder.statusSpinner.setSelection(pos);

            holder.btnSave.setOnClickListener(v -> {
                String newStatus = holder.statusSpinner.getSelectedItem().toString();
                listener.onStatusChange(r, newStatus);
                editingId = -1;
            });

            holder.btnCancel.setOnClickListener(v -> {
                editingId = -1;
                notifyItemChanged(position);
            });

        } else {
            holder.statusView.setVisibility(View.VISIBLE);
            holder.statusEditContainer.setVisibility(View.GONE);

            holder.statusView.setText(r.getStatus().toString());
            holder.statusView.setOnClickListener(v -> {
                editingId = r.getId();
                notifyItemChanged(position);
            });
        }
    }

    @Override
    public int getItemCount() {
        return reservations.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtId, txtDate, txtStart, txtEnd, txtService, txtEvent, statusView;
        View statusEditContainer;
        Spinner statusSpinner;
        ImageButton btnSave, btnCancel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtId = itemView.findViewById(R.id.txtId);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtStart = itemView.findViewById(R.id.txtStart);
            txtEnd = itemView.findViewById(R.id.txtEnd);
            txtService = itemView.findViewById(R.id.txtService);
            txtEvent = itemView.findViewById(R.id.txtEvent);
            statusView = itemView.findViewById(R.id.statusView);

            statusEditContainer = itemView.findViewById(R.id.statusEditContainer);
            statusSpinner = itemView.findViewById(R.id.statusSpinner);
            btnSave = itemView.findViewById(R.id.btnSave);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }
    }
}
