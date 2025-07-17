package com.eventplanner.model.responses.solutionCateogries;

import com.eventplanner.model.enums.RequestStatus;

public class GetSolutionCategoryResponse {

    private Long id;
    private String name;
    private String description;


    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

}
