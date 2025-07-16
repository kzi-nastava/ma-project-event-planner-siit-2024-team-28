package com.eventplanner.model.requests.eventTypes;

import java.util.List;

public class CreateEventTypeRequest {
    private String name;
    private String description;
    private List<Long> recommendedSolutionCategoryIds;

    public CreateEventTypeRequest() {}

    public CreateEventTypeRequest(String name, String description, List<Long> categoryIds) {
        this.name = name;
        this.description = description;
        this.recommendedSolutionCategoryIds = categoryIds;
    }
}