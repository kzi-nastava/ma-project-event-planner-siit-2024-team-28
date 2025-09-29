package com.eventplanner.model.responses.calendar;

import java.util.List;

public class CalendarResponseDTO {
    private List<CalendarEventDTO> events;

    public CalendarResponseDTO() {}

    public CalendarResponseDTO(List<CalendarEventDTO> events) {
        this.events = events;
    }

    public List<CalendarEventDTO> getEvents() {
        return events;
    }

    public void setEvents(List<CalendarEventDTO> events) {
        this.events = events;
    }
}
