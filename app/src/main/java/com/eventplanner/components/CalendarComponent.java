package com.eventplanner.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eventplanner.R;
import com.eventplanner.adapters.calendar.CalendarAdapter;
import com.eventplanner.model.constants.UserRoles;
import com.eventplanner.model.responses.calendar.CalendarEventDTO;
import com.eventplanner.model.responses.calendar.CalendarResponseDTO;
import com.eventplanner.services.CalendarService;
import com.eventplanner.utils.AuthUtils;
import com.eventplanner.utils.HttpUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CalendarComponent extends LinearLayout {
    
    private TextView tvMonthYear;
    private Button btnPreviousMonth, btnNextMonth;
    private RecyclerView rvCalendarGrid;
    private CalendarAdapter calendarAdapter;
    
    private Calendar currentCalendar;
    private List<CalendarEventDTO> allEvents;
    private OnEventClickListener eventClickListener;
    
    public interface OnEventClickListener {
        void onEventClick(CalendarEventDTO event);
    }
    
    public CalendarComponent(Context context) {
        super(context);
        init(context);
    }
    
    public CalendarComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    public CalendarComponent(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
    
    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.component_calendar, this, true);
        
        currentCalendar = Calendar.getInstance();
        allEvents = new ArrayList<>();
        
        initViews();
        setupRecyclerView();
        setupClickListeners();
        updateCalendarDisplay();
        loadCalendarEvents();
    }
    
    private void initViews() {
        tvMonthYear = findViewById(R.id.tvMonthYear);
        btnPreviousMonth = findViewById(R.id.btnPreviousMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);
        rvCalendarGrid = findViewById(R.id.rvCalendarGrid);
    }
    
    private void setupRecyclerView() {
        calendarAdapter = new CalendarAdapter(getContext());
        rvCalendarGrid.setLayoutManager(new GridLayoutManager(getContext(), 7));
        rvCalendarGrid.setAdapter(calendarAdapter);
        
        calendarAdapter.setEventClickListener(event -> {
            if (eventClickListener != null) {
                eventClickListener.onEventClick(event);
            }
        });
    }
    
    private void setupClickListeners() {
        btnPreviousMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            updateCalendarDisplay();
        });
        
        btnNextMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            updateCalendarDisplay();
        });
    }
    
    private void updateCalendarDisplay() {
        // Update month/year text
        String monthYear = currentCalendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) +
                " " + currentCalendar.get(Calendar.YEAR);
        tvMonthYear.setText(monthYear);

        // Update calendar grid - always pass events list (even if empty)
        int year = currentCalendar.get(Calendar.YEAR);
        int month = currentCalendar.get(Calendar.MONTH);
        List<CalendarEventDTO> eventsToShow = allEvents != null ? allEvents : new ArrayList<>();
        calendarAdapter.updateCalendar(year, month, eventsToShow);
    }
    
    public void setOnEventClickListener(OnEventClickListener listener) {
        this.eventClickListener = listener;
    }
    
    public void loadCalendarEvents() {
        Context context = getContext();
        List<String> userRoles = AuthUtils.getUserRoles(context);

        // Clear existing events
        allEvents.clear();

        // Always update calendar display first to show the grid
        updateCalendarDisplay();

        if (userRoles == null || userRoles.isEmpty()) {
            return;
        }

        CalendarService calendarService = HttpUtils.getCalendarService();

        // Load accepted events for all logged-in users (including admins)
        loadAcceptedEvents(calendarService);

        // Load role-specific events
        if (userRoles.contains(UserRoles.EventOrganizer)) {
            loadCreatedEvents(calendarService);
        }

        if (userRoles.contains(UserRoles.BusinessOwner)) {
            loadServiceReservations(calendarService);
        }
    }
    
    private void loadAcceptedEvents(CalendarService calendarService) {
        Call<CalendarResponseDTO> call = calendarService.getCurrentUserAcceptedEvents();
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<CalendarResponseDTO> call, @NonNull Response<CalendarResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<CalendarEventDTO> events = response.body().getEvents();
                    if (events != null) {
                        allEvents.addAll(events);
                        updateCalendarDisplay();
                    }
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<CalendarResponseDTO> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), R.string.failed_to_load_accepted_events, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadCreatedEvents(CalendarService calendarService) {
        Call<CalendarResponseDTO> call = calendarService.getCurrentEventOrganizerCreatedEvents();
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<CalendarResponseDTO> call, @NonNull Response<CalendarResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<CalendarEventDTO> events = response.body().getEvents();
                    if (events != null) {
                        allEvents.addAll(events);
                        updateCalendarDisplay();
                    }
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<CalendarResponseDTO> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), R.string.failed_to_load_created_events, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadServiceReservations(CalendarService calendarService) {
        Call<CalendarResponseDTO> call = calendarService.getCurrentBusinessOwnerServiceReservations();
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<CalendarResponseDTO> call, @NonNull Response<CalendarResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<CalendarEventDTO> events = response.body().getEvents();
                    if (events != null) {
                        allEvents.addAll(events);
                        updateCalendarDisplay();
                    }
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<CalendarResponseDTO> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), R.string.failed_to_load_service_reservations, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    public void refreshEvents() {
        allEvents.clear();
        loadCalendarEvents();
    }
}
