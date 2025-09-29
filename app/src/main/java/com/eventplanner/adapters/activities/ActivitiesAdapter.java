package com.eventplanner.adapters.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eventplanner.databinding.ItemActivityBinding;
import com.eventplanner.model.requests.activities.CreateActivityRequest;
import com.eventplanner.utils.SimpleTextWatcher;
import com.google.android.material.textfield.TextInputEditText;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class ActivitiesAdapter extends RecyclerView.Adapter<ActivitiesAdapter.ActivityViewHolder> {
    private final List<CreateActivityRequest> activities;
    private final boolean isReadOnly;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ActivitiesAdapter(List<CreateActivityRequest> activities, boolean isReadOnly) {
        this.activities = activities;
        this.isReadOnly = isReadOnly;
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemActivityBinding binding = ItemActivityBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ActivityViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        CreateActivityRequest activity = activities.get(position);
        holder.bind(activity, position);
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    class ActivityViewHolder extends RecyclerView.ViewHolder {
        private final ItemActivityBinding binding;

        public ActivityViewHolder(ItemActivityBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(CreateActivityRequest activity, int position) {
            binding.name.setText(activity.getName());
            binding.description.setText(activity.getDescription());
            binding.location.setText(activity.getLocation());

            // Handle null dates more robustly
            String startTimeText = "";
            if (activity.getStartTime() != null) {
                try {
                    startTimeText = activity.getStartTime().format(formatter);
                } catch (Exception e) {
                    startTimeText = "";
                }
            }
            binding.startTime.setText(startTimeText);

            String endTimeText = "";
            if (activity.getEndTime() != null) {
                try {
                    endTimeText = activity.getEndTime().format(formatter);
                } catch (Exception e) {
                    endTimeText = "";
                }
            }
            binding.endTime.setText(endTimeText);

            // Disable fields if read-only
            if (isReadOnly) {
                binding.name.setEnabled(false);
                binding.description.setEnabled(false);
                binding.location.setEnabled(false);
                binding.startTime.setEnabled(false);
                binding.endTime.setEnabled(false);
                binding.removeButton.setVisibility(View.GONE);
            }

            // Set listeners
            binding.name.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    activity.setName(s.toString());
                }
            });

            binding.description.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    activity.setDescription(s.toString());
                }
            });

            binding.location.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    activity.setLocation(s.toString());
                }
            });

            binding.startTime.setFocusable(false);
            binding.startTime.setClickable(true);
            binding.startTime.setOnClickListener(v -> showDateTimePicker(v.getContext(), binding.startTime, formatter, activity::setStartTime));

            binding.endTime.setFocusable(false);
            binding.endTime.setClickable(true);
            binding.endTime.setOnClickListener(v -> showDateTimePicker(v.getContext(), binding.endTime, formatter, activity::setEndTime));


            binding.removeButton.setOnClickListener(v -> {
                if (!isReadOnly) {
                    activities.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, activities.size());
                }
            });
        }
    }

    private void showDateTimePicker(Context context, TextInputEditText editText, DateTimeFormatter formatter, java.util.function.Consumer<LocalDateTime> onPicked) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        if (!editText.getText().toString().isEmpty()) {
            try {
                currentDateTime = LocalDateTime.parse(editText.getText().toString(), formatter);
            } catch (DateTimeParseException ignored) {
            }
        }

        LocalDateTime finalCurrentDateTime = currentDateTime;
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                context,
                (dateView, year, month, dayOfMonth) -> {
                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                            context,
                            (timeView, hourOfDay, minute) -> {
                                LocalDateTime selectedDateTime = LocalDateTime.of(
                                        year, month + 1, dayOfMonth, hourOfDay, minute
                                );
                                editText.setText(selectedDateTime.format(formatter));
                                if (onPicked != null) onPicked.accept(selectedDateTime);
                            },
                            finalCurrentDateTime.getHour(),
                            finalCurrentDateTime.getMinute(),
                            true
                    );
                    timePickerDialog.show();
                },
                currentDateTime.getYear(),
                currentDateTime.getMonthValue() - 1,
                currentDateTime.getDayOfMonth()
        );
        datePickerDialog.show();
    }
}