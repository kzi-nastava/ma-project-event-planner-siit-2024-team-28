package com.eventplanner.model.requests.events;

import com.eventplanner.model.enums.PrivacyType;
import com.eventplanner.model.requests.activities.CreateActivityRequest;
import com.eventplanner.model.requests.locations.CreateLocationRequest;

import java.time.LocalDate;
import java.util.List;

public class CreateEventRequest {
    private String name;
    private String description;
    private Integer maxParticipants;
    private PrivacyType privacyType;
    private Long eventTypeId;
    private CreateLocationRequest location;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<CreateActivityRequest> activities;
    private String imageBase64;

    public CreateEventRequest(String name, String description, Integer maxParticipants, PrivacyType privacyType, Long eventTypeId, CreateLocationRequest location, LocalDate startDate, LocalDate endDate, List<CreateActivityRequest> activities, String imageBase64) {
        this.name = name;
        this.description = description;
        this.maxParticipants = maxParticipants;
        this.privacyType = privacyType;
        this.eventTypeId = eventTypeId;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        this.activities = activities;
        this.imageBase64 = imageBase64;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public PrivacyType getPrivacyType() {
        return privacyType;
    }

    public void setPrivacyType(PrivacyType privacyType) {
        this.privacyType = privacyType;
    }

    public Long getEventTypeId() {
        return eventTypeId;
    }

    public void setEventTypeId(Long eventTypeId) {
        this.eventTypeId = eventTypeId;
    }

    public CreateLocationRequest getLocation() {
        return location;
    }

    public void setLocation(CreateLocationRequest location) {
        this.location = location;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public List<CreateActivityRequest> getActivities() {
        return activities;
    }

    public void setActivities(List<CreateActivityRequest> activities) {
        this.activities = activities;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }
}
