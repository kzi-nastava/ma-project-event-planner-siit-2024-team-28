package com.eventplanner.model.requests.solutionCategories;

import com.eventplanner.model.enums.RequestStatus;

public class CreateSolutionCategoryRequest {
    String name;
    String description;

    public CreateSolutionCategoryRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
