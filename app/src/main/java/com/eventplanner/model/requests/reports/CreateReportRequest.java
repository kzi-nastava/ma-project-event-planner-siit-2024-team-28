package com.eventplanner.model.requests.reports;

public class CreateReportRequest {
    String description;
    Long reportedUserId;

    public CreateReportRequest(Long reportedUserId, String description) {
        this.reportedUserId = reportedUserId;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public Long getReportedUserId() {
        return reportedUserId;
    }
}
