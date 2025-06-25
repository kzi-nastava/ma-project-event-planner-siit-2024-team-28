package com.eventplanner.model.requests.reports;

public class CreateReportRequest {
    String description;
    Long reportedUserId;

    public String getDescription() {
        return description;
    }

    public Long getReportedUserId() {
        return reportedUserId;
    }
}
