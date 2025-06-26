package com.eventplanner.model.responses.solutionCateogries;

import com.eventplanner.model.enums.RequestStatus;

public class GetSolutionCategoryResponse {

    private Long id;
    private String name;
    private String description;
    private RequestStatus requestStatus;
    private Boolean isDeleted;


    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public RequestStatus getRequestStatus() {
        return requestStatus;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }
}
