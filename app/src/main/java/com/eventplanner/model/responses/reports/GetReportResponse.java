package com.eventplanner.model.responses.reports;

import com.eventplanner.model.enums.RequestStatus;

public class GetReportResponse {
    Long id;
    String description;
    Long reportedUserId;
    RequestStatus status;
    Boolean isDeleted;

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public Long getReportedUserId() {
        return reportedUserId;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }
}
