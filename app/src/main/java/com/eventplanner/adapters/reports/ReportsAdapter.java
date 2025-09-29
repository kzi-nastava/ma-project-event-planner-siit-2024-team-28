package com.eventplanner.adapters.reports;

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
import com.eventplanner.model.responses.reports.GetReportResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.ReportViewHolder> {

    public interface OnEditStatusClickListener {
        void onEditStatusClick(GetReportResponse report, String newStatus);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(GetReportResponse report);
    }

    public interface OnUserClickListener {
        void onUserClick(long userId);
    }

    private final List<GetReportResponse> reports;
    private final OnEditStatusClickListener editListener;
    private final OnDeleteClickListener deleteListener;
    private final OnUserClickListener userClickListener;

    private long editingReportId = -1;

    private final String[] statuses = {"PENDING", "ACCEPTED", "REJECTED"};

    public ReportsAdapter(List<GetReportResponse> reports,
                          OnEditStatusClickListener editListener,
                          OnDeleteClickListener deleteListener,
                          OnUserClickListener userClickListener) {
        this.reports = reports;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
        this.userClickListener = userClickListener;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        GetReportResponse report = reports.get(position);

        holder.idText.setText("#" + report.getId());
        holder.descriptionText.setText(report.getDescription());
        holder.userIdText.setText(String.valueOf(report.getReportedUserId()));
        holder.userIdText.setOnClickListener(v -> userClickListener.onUserClick(report.getReportedUserId()));

        holder.isSuspendedText.setText(String.valueOf(report.getIsSuspended()));
        holder.suspendedOnText.setText(formatDate(report.getSuspensionTimestamp()));
        holder.suspendedUntilText.setText(formatDate(report.getSuspensionTimestamp() + 86400000L * 3));

        if (editingReportId == report.getId()) {
            holder.statusText.setVisibility(View.GONE);
            holder.statusSpinner.setVisibility(View.VISIBLE);
            holder.saveButton.setVisibility(View.VISIBLE);
            holder.cancelButton.setVisibility(View.VISIBLE);

            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(holder.itemView.getContext(),
                    android.R.layout.simple_spinner_item, statuses);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            holder.statusSpinner.setAdapter(spinnerAdapter);
            holder.statusSpinner.setSelection(getStatusIndex(report.getStatus().toString()));

            holder.saveButton.setOnClickListener(v -> {
                String newStatus = (String) holder.statusSpinner.getSelectedItem();
                editListener.onEditStatusClick(report, newStatus);
                editingReportId = -1;
                notifyDataSetChanged();
            });

            holder.cancelButton.setOnClickListener(v -> {
                editingReportId = -1;
                notifyDataSetChanged();
            });

        } else {
            holder.statusText.setVisibility(View.VISIBLE);
            holder.statusSpinner.setVisibility(View.GONE);
            holder.saveButton.setVisibility(View.GONE);
            holder.cancelButton.setVisibility(View.GONE);
            holder.statusText.setText(report.getStatus().toString());

            holder.statusText.setOnClickListener(v -> {
                editingReportId = report.getId();
                notifyDataSetChanged();
            });
        }

        holder.deleteButton.setOnClickListener(v -> deleteListener.onDeleteClick(report));
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    private int getStatusIndex(String status) {
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i].equals(status)) return i;
        }
        return 0;
    }

    private String formatDate(long timestamp) {
        if (timestamp <= 86400000L * 3) return "/"; // replicate Angular logic
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy - HH:mm", Locale.getDefault());
        return sdf.format(date);
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView idText, descriptionText, userIdText, isSuspendedText, suspendedOnText, suspendedUntilText, statusText;
        Spinner statusSpinner;
        ImageButton saveButton, cancelButton, deleteButton;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            idText = itemView.findViewById(R.id.report_id);
            descriptionText = itemView.findViewById(R.id.report_description);
            userIdText = itemView.findViewById(R.id.report_user_id);
            isSuspendedText = itemView.findViewById(R.id.report_is_suspended);
            suspendedOnText = itemView.findViewById(R.id.report_suspended_on);
            suspendedUntilText = itemView.findViewById(R.id.report_suspended_until);
            statusText = itemView.findViewById(R.id.report_status_text);
            statusSpinner = itemView.findViewById(R.id.report_status_spinner);
            saveButton = itemView.findViewById(R.id.save_status_button);
            cancelButton = itemView.findViewById(R.id.cancel_status_button);
            deleteButton = itemView.findViewById(R.id.delete_report_button);
        }
    }
}
