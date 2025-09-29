package com.eventplanner.model.requests.solutionCategories;

import com.eventplanner.model.enums.RequestStatus;

public class CategoryAcceptionRequest {
    private Long id;
    private String name;
    private String description;
    private RequestStatus status;

    public CategoryAcceptionRequest(Long id, String name, String description, RequestStatus status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
    }
}
