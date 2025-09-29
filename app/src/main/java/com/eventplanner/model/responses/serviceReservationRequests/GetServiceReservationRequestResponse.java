package com.eventplanner.model.responses.serviceReservationRequests;

import com.eventplanner.model.enums.RequestStatus;

public class GetServiceReservationRequestResponse {
    Long id;
    String date;
    String startTime;
    String endTime;
    RequestStatus status;
    Boolean isDeleted;
    Long serviceId;
    Long eventId;

    public Long getId() {
        return id;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    public Long getEventId() {
        return eventId;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public String getDate() {
        return date;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }
}
