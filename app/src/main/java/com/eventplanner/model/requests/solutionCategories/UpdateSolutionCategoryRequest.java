package com.eventplanner.model.requests.solutionCategories;

import com.eventplanner.model.enums.RequestStatus;

public class UpdateSolutionCategoryRequest {
    private String name;
    private String description;

    public UpdateSolutionCategoryRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
