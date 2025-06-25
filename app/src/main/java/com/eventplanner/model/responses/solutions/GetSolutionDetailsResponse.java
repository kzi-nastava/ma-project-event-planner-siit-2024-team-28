package com.eventplanner.model.responses.solutions;

import com.eventplanner.model.enums.ReservationType;

import java.util.Collection;

public class GetSolutionDetailsResponse {
    private Long id;
    private String type;
    private String name;
    private String description;
    private Double price;
    private Double discount;
    private String imageBase64;
    private Boolean isDeleted;
    private Boolean isVisibleForEventOrganizers;
    private Boolean isAvailable;
    private String specifics;
    private Integer fixedDurationInSeconds;
    private Integer minDurationInSeconds;
    private Integer maxDurationInSeconds;
    private Integer reservationDeadlineDays;
    private Integer cancellationDeadlineDays;
    private ReservationType reservationType;
    private Long categoryId;
    private String categoryName;
    private Long businessOwnerId;
    private String businessOwnerName;
    private Collection<Long> eventTypeIds;
    private Collection<String> eventTypeNames;

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
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

    public String getImageBase64() {
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

    public String getCategoryName() {
        return categoryName;
    }

    public Long getBusinessOwnerId() {
        return businessOwnerId;
    }

    public String getBusinessOwnerName() {
        return businessOwnerName;
    }

    public Collection<Long> getEventTypeIds() {
        return eventTypeIds;
    }

    public Collection<String> getEventTypeNames() {
        return eventTypeNames;
    }
}
