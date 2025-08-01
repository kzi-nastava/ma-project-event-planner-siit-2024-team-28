package com.eventplanner.model.responses.events;

import com.eventplanner.model.responses.activities.GetActivityResponse;
import com.eventplanner.model.responses.locations.GetLocationResponse;

import java.util.List;

public class GetEventResponse {
    private Long id;
    private String name;
    private String description;
    private Integer maxParticipants;
    private String privacyType;
    private String startDate;
    private String endDate;
    private String imageBase64;
    private Long eventOrganizerId;
    private Long eventTypeId;
    private String eventTypeName; // for filters
    private GetLocationResponse location;
    private List<GetActivityResponse> activities;
    private List<Long> requiredSolutionIds;
    private boolean isFavoriteForCurrentUser;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getPrivacyType() {
        return privacyType;
    }

    public void setPrivacyType(String privacyType) {
        this.privacyType = privacyType;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public Long getEventOrganizerId() {
        return eventOrganizerId;
    }

    public void setEventOrganizerId(Long eventOrganizerId) {
        this.eventOrganizerId = eventOrganizerId;
    }

    public Long getEventTypeId() {
        return eventTypeId;
    }

    public void setEventTypeId(Long eventTypeId) {
        this.eventTypeId = eventTypeId;
    }

    public String getEventTypeName() {
        return eventTypeName;
    }

    public void setEventTypeName(String eventTypeName) {
        this.eventTypeName = eventTypeName;
    }

    public GetLocationResponse getLocation() {
        return location;
    }

    public void setLocation(GetLocationResponse location) {
        this.location = location;
    }

    public List<GetActivityResponse> getActivities() {
        return activities;
    }

    public void setActivities(List<GetActivityResponse> activities) {
        this.activities = activities;
    }

    public List<Long> getRequiredSolutionIds() {
        return requiredSolutionIds;
    }

    public void setRequiredSolutionIds(List<Long> requiredSolutionIds) {
        this.requiredSolutionIds = requiredSolutionIds;
    }

    public boolean isFavoriteForCurrentUser() {
        return isFavoriteForCurrentUser;
    }

    public void setFavoriteForCurrentUser(boolean favoriteForCurrentUser) {
        isFavoriteForCurrentUser = favoriteForCurrentUser;
    }
}
