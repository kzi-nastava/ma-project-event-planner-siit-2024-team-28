package com.eventplanner.model.requests.solutionCategories;

import com.eventplanner.model.enums.RequestStatus;

public class CreateSolutionCategoryRequest {
    String name;
    String description;
    RequestStatus status;

    public CreateSolutionCategoryRequest(String name, String description, RequestStatus status) {
        this.name = name;
        this.description = description;
        this.status = status;
    }
}
