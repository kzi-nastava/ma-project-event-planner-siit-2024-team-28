package com.eventplanner.model.responses.eventTypes;

import com.eventplanner.model.responses.solutionCateogries.GetSolutionCategoryResponse;

import java.util.Collection;

public class GetEventTypeResponse {
    private Long id;
    private String name;
    private String description;
    private Boolean isActive;
    private Collection<GetSolutionCategoryResponse> recommendedSolutionCategories;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public Collection<GetSolutionCategoryResponse> getRecommendedSolutionCategories() {
        return recommendedSolutionCategories;
    }
}
