package com.eventplanner.model.responses.services;

import com.eventplanner.model.enums.DurationType;
import com.eventplanner.model.enums.ReservationType;
import com.eventplanner.model.enums.SolutionStatus;

import java.util.Collection;
import java.util.List;

public class GetServiceResponse {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Double discount;
    private List<String> imageBase64;
    private Boolean isDeleted;
    private Boolean isVisibleForEventOrganizers;
    private Boolean isAvailable;
    private String specifics;
    private DurationType durationType;
    private Integer fixedDurationInSeconds;
    private Integer minDurationInSeconds;
    private Integer maxDurationInSeconds;
    private Integer reservationDeadlineDays;
    private Integer cancellationDeadlineDays;
    private ReservationType reservationType;
    private Long categoryId;
    private Long businessOwnerId;
    private SolutionStatus status;
    private Collection<Long> eventTypeIds;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Double getPrice() {
        return price;
    }

    public Double getDiscount() {
        return discount;
    }

    public List<String> getImageBase64() {
        return imageBase64;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public Boolean getIsVisibleForEventOrganizers() {
        return isVisibleForEventOrganizers;
    }

    public Boolean getIsAvailable() {
        return isAvailable;
    }

    public String getSpecifics() {
        return specifics;
    }

    public DurationType getDurationType() {return durationType; }

    public Integer getFixedDurationInSeconds() {
        return fixedDurationInSeconds;
    }

    public Integer getMinDurationInSeconds() {
        return minDurationInSeconds;
    }

    public Integer getMaxDurationInSeconds() {
        return maxDurationInSeconds;
    }

    public Integer getReservationDeadlineDays() {
        return reservationDeadlineDays;
    }

    public Integer getCancellationDeadlineDays() {
        return cancellationDeadlineDays;
    }

    public ReservationType getReservationType() {
        return reservationType;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public Long getBusinessOwnerId() {
        return businessOwnerId;
    }

    public SolutionStatus getStatus() {
        return status;
    }

    public Collection<Long> getEventTypeIds() {
        return eventTypeIds;
    }
}
