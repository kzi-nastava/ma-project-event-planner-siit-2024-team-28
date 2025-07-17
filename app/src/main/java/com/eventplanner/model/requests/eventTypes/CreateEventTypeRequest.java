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

    public List<Long> getRecommendedSolutionCategoryIds() {
        return recommendedSolutionCategoryIds;
    }

    public void setRecommendedSolutionCategoryIds(List<Long> recommendedSolutionCategoryIds) {
        this.recommendedSolutionCategoryIds = recommendedSolutionCategoryIds;
    }
}