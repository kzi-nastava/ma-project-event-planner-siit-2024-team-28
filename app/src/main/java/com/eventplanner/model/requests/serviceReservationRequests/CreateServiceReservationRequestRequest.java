package com.eventplanner.model.requests.serviceReservationRequests;

import com.eventplanner.model.enums.RequestStatus;
import com.eventplanner.model.requests.requiredSolutions.CreateRequiredSolutionRequest;

public class CreateServiceReservationRequestRequest {
    String date;
    String startTime;
    String endTime;
    Long serviceId;
    Long eventId;

    public CreateServiceReservationRequestRequest(String date, String startTime, String endTime, Long serviceId, Long eventId)
    {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.serviceId = serviceId;
        this.eventId = eventId;
    }

    public Long getEventId() {
        return eventId;
    }

    public Long getServiceId() {
        return serviceId;
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

}
