package com.eventplanner.model.requests.solutionCategories;

import com.eventplanner.model.enums.RequestStatus;

public class UpdateSolutionCategoryRequest {
    private String name;
    private String description;
    private RequestStatus status;

    public UpdateSolutionCategoryRequest(String name, String description, RequestStatus status) {
        this.name = name;
        this.description = description;
        this.status = status;
    }
}
