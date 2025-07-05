package com.eventplanner.model.responses.events;

import com.eventplanner.model.enums.PrivacyType;

import java.time.LocalDate;
import java.util.List;

public class GetEventResponse {
    private Long id;
    private String name;
    private String description;
    private int maxParticipants;
    private PrivacyType privacyType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String imageBase64;
    private Long eventOrganizerId;
    private Long eventTypeId;
    private Long locationId;
    private List<Long> activityIds;
    private List<Long> requiredSolutionIds;

    // Getters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public PrivacyType getPrivacyType() {
        return privacyType;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public Long getEventOrganizerId() {
        return eventOrganizerId;
    }

    public Long getEventTypeId() {
        return eventTypeId;
    }

    public Long getLocationId() {
        return locationId;
    }

    public List<Long> getActivityIds() {
        return activityIds;
    }

    public List<Long> getRequiredSolutionIds() {
        return requiredSolutionIds;
    }
}
