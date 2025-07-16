package com.eventplanner.model.requests.eventTypes;

import java.util.List;

public class UpdateEventTypeRequest {
    private String description;
    private List<Long> recommendedSolutionCategoryIds;

    public UpdateEventTypeRequest() {}

    public UpdateEventTypeRequest(String description, List<Long> categoryIds) {
        this.description = description;
        this.recommendedSolutionCategoryIds = categoryIds;
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