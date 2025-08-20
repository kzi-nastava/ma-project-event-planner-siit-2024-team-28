package com.eventplanner.adapters.calendar;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eventplanner.R;
import com.eventplanner.model.enums.CalendarEventType;
import com.eventplanner.model.responses.calendar.CalendarEventDTO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {
    
    private Context context;
    private List<CalendarDay> calendarDays;
    private OnEventClickListener eventClickListener;
    
    public interface OnEventClickListener {
        void onEventClick(CalendarEventDTO event);
    }
    
    public CalendarAdapter(Context context) {
        this.context = context;
        this.calendarDays = new ArrayList<>();
    }
    
    public void setEventClickListener(OnEventClickListener listener) {
        this.eventClickListener = listener;
    }
    
    public void updateCalendar(int year, int month, List<CalendarEventDTO> events) {
        calendarDays.clear();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1);

        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Add empty days for previous month
        for (int i = 0; i < firstDayOfWeek; i++) {
            calendarDays.add(new CalendarDay(0, false, new ArrayList<>()));
        }

        // Add days of current month
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate currentDate = LocalDate.of(year, month + 1, day);
            List<CalendarEventDTO> dayEvents = events != null ? getEventsForDate(events, currentDate) : new ArrayList<>();
            calendarDays.add(new CalendarDay(day, true, dayEvents));
        }

        // Add empty days for next month to complete the grid (6 rows x 7 days = 42 total)
        int totalCells = 42;
        int currentCells = calendarDays.size();
        for (int i = currentCells; i < totalCells; i++) {
            calendarDays.add(new CalendarDay(0, false, new ArrayList<>()));
        }

        notifyDataSetChanged();
    }
    
    private List<CalendarEventDTO> getEventsForDate(List<CalendarEventDTO> allEvents, LocalDate date) {
        List<CalendarEventDTO> dayEvents = new ArrayList<>();
        if (allEvents != null) {
            for (CalendarEventDTO event : allEvents) {
                if (isEventOnDate(event, date)) {
                    dayEvents.add(event);
                }
            }
        }
        return dayEvents;
    }
    
    private boolean isEventOnDate(CalendarEventDTO event, LocalDate date) {
        LocalDate startDate = event.getStartDate();
        LocalDate endDate = event.getEndDate() != null ? event.getEndDate() : startDate;
        
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }
    
    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_calendar_day, parent, false);
        return new CalendarViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        CalendarDay calendarDay = calendarDays.get(position);
        holder.bind(calendarDay);
    }
    
    @Override
    public int getItemCount() {
        return calendarDays.size();
    }
    
    class CalendarViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayNumber;
        LinearLayout llEventsContainer;
        TextView tvEventCount;

        public CalendarViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayNumber = itemView.findViewById(R.id.tvDayNumber);
            llEventsContainer = itemView.findViewById(R.id.llEventsContainer);
            tvEventCount = itemView.findViewById(R.id.tvEventCount);
        }
        
        public void bind(CalendarDay calendarDay) {
            if (calendarDay.isCurrentMonth) {
                tvDayNumber.setText(String.valueOf(calendarDay.dayNumber));
                tvDayNumber.setTextColor(Color.BLACK);
            } else {
                tvDayNumber.setText("");
                tvDayNumber.setTextColor(Color.GRAY);
            }

            // Clear previous events
            llEventsContainer.removeAllViews();
            tvEventCount.setVisibility(View.GONE);

            // Add events for this day (limit to 3 visible events)
            int maxVisibleEvents = 3;
            int eventCount = calendarDay.events.size();

            for (int i = 0; i < Math.min(eventCount, maxVisibleEvents); i++) {
                addEventView(calendarDay.events.get(i));
            }

            // Show event count if there are more events
            if (eventCount > maxVisibleEvents) {
                tvEventCount.setText("+" + (eventCount - maxVisibleEvents) + " more");
                tvEventCount.setVisibility(View.VISIBLE);

                // Make the entire day clickable to show all events
                itemView.setOnClickListener(v -> showAllEventsDialog(calendarDay.events));
            } else {
                itemView.setOnClickListener(null);
            }
        }
        
        private void addEventView(CalendarEventDTO event) {
            View eventView = LayoutInflater.from(context).inflate(R.layout.item_calendar_event, llEventsContainer, false);
            TextView tvEventTitle = eventView.findViewById(R.id.tvEventTitle);

            // Format event title with time if available
            String displayText = event.getTitle();
            if (event.getStartTime() != null) {
                displayText = event.getStartTime().toString().substring(0, 5) + " " + event.getTitle();
            }
            tvEventTitle.setText(displayText);

            // Set background color based on event type
            int backgroundColor = getEventColor(event.getType());
            tvEventTitle.setBackgroundColor(backgroundColor);

            // Add visual indicator for event type
            String typeIndicator = getEventTypeIndicator(event.getType());
            if (!typeIndicator.isEmpty()) {
                displayText = typeIndicator + " " + displayText;
                tvEventTitle.setText(displayText);
            }

            eventView.setOnClickListener(v -> {
                if (eventClickListener != null) {
                    eventClickListener.onEventClick(event);
                }
            });

            llEventsContainer.addView(eventView);
        }
        
        private int getEventColor(CalendarEventType type) {
            switch (type) {
                case EVENT:
                    return Color.parseColor("#2196F3"); // Blue
                case CREATED_EVENT:
                    return Color.parseColor("#4CAF50"); // Green
                case SERVICE_RESERVATION:
                    return Color.parseColor("#FF9800"); // Orange
                default:
                    return Color.parseColor("#9C27B0"); // Purple
            }
        }

        private String getEventTypeIndicator(CalendarEventType type) {
            switch (type) {
                case EVENT:
                    return "📅"; // Calendar emoji for accepted events
                case CREATED_EVENT:
                    return "✨"; // Sparkles emoji for created events
                case SERVICE_RESERVATION:
                    return "🔧"; // Wrench emoji for service reservations
                default:
                    return "";
            }
        }

        private void showAllEventsDialog(List<CalendarEventDTO> events) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Events for this day");

            StringBuilder message = new StringBuilder();
            for (int i = 0; i < events.size(); i++) {
                CalendarEventDTO event = events.get(i);
                message.append(getEventTypeIndicator(event.getType()))
                       .append(" ")
                       .append(event.getTitle());

                if (event.getStartTime() != null) {
                    message.append(" (").append(event.getStartTime().toString().substring(0, 5)).append(")");
                }

                if (i < events.size() - 1) {
                    message.append("\n");
                }
            }

            builder.setMessage(message.toString());
            builder.setPositiveButton("OK", null);
            builder.show();
        }
    }
    
    static class CalendarDay {
        int dayNumber;
        boolean isCurrentMonth;
        List<CalendarEventDTO> events;
        
        public CalendarDay(int dayNumber, boolean isCurrentMonth, List<CalendarEventDTO> events) {
            this.dayNumber = dayNumber;
            this.isCurrentMonth = isCurrentMonth;
            this.events = events;
        }
    }
}
