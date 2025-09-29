package com.eventplanner.model.responses.reports;

import com.eventplanner.model.enums.RequestStatus;

public class GetReportResponse {
    Long id;
    String description;
    Long reportedUserId;
    RequestStatus status;
    Boolean isDeleted;
    Long suspensionTimestamp;
    Boolean isSuspended;

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
    public Long getSuspensionTimestamp() { return suspensionTimestamp;}

    public void setSuspended(Boolean suspended) {
        isSuspended = suspended;
    }

    public void setSuspensionTimestamp(Long suspensionTimestamp) {
        this.suspensionTimestamp = suspensionTimestamp;
    }

    public Boolean getIsSuspended() { return isSuspended;}

    public void setStatus(String newStatus) {
        this.status = RequestStatus.valueOf(newStatus);
    }
}
