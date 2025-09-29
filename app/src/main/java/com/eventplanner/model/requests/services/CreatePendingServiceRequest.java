package com.eventplanner.model.requests.services;

import com.eventplanner.model.enums.DurationType;
import com.eventplanner.model.enums.ReservationType;

import java.util.Collection;
import java.util.List;

public class CreatePendingServiceRequest {
    private String name;
    private String description;
    private Double price;
    private Double discount;
    private List<String> imagesBase64;
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
    private String categoryName;
    private Long businessOwnerId;
    private Collection<Long> eventTypeIds;

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    private CreatePendingServiceRequest(CreatePendingServiceRequest.Builder builder) {
        this.name = builder.name;
        this.description = builder.description;
        this.price = builder.price;
        this.discount = builder.discount;
        this.imagesBase64 = builder.imageBase64;
        this.isVisibleForEventOrganizers = builder.isVisibleForEventOrganizers;
        this.isAvailable = builder.isAvailable;
        this.specifics = builder.specifics;
        this.durationType = builder.durationType;
        this.fixedDurationInSeconds = builder.fixedDurationInSeconds;
        this.minDurationInSeconds = builder.minDurationInSeconds;
        this.maxDurationInSeconds = builder.maxDurationInSeconds;
        this.reservationDeadlineDays = builder.reservationDeadlineDays;
        this.cancellationDeadlineDays = builder.cancellationDeadlineDays;
        this.reservationType = builder.reservationType;
        this.categoryName = builder.categoryName;
        this.businessOwnerId = builder.businessOwnerId;
        this.eventTypeIds = builder.eventTypeIds;
    }

    // Static Builder class
    public static class Builder {
        private String name;
        private String description;
        private Double price;
        private Double discount;
        private List<String> imageBase64;
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
        private String categoryName;
        private Long businessOwnerId;
        private Collection<Long> eventTypeIds;

        public CreatePendingServiceRequest.Builder name(String name) {
            this.name = name;
            return this;
        }

        public CreatePendingServiceRequest.Builder description(String description) {
            this.description = description;
            return this;
        }

        public CreatePendingServiceRequest.Builder price(Double price) {
            this.price = price;
            return this;
        }

        public CreatePendingServiceRequest.Builder discount(Double discount) {
            this.discount = discount;
            return this;
        }

        public CreatePendingServiceRequest.Builder imageBase64(List<String> imageBase64) {
            this.imageBase64 = imageBase64;
            return this;
        }

        public CreatePendingServiceRequest.Builder isVisibleForEventOrganizers(Boolean isVisibleForEventOrganizers) {
            this.isVisibleForEventOrganizers = isVisibleForEventOrganizers;
            return this;
        }

        public CreatePendingServiceRequest.Builder isAvailable(Boolean isAvailable) {
            this.isAvailable = isAvailable;
            return this;
        }

        public CreatePendingServiceRequest.Builder specifics(String specifics) {
            this.specifics = specifics;
            return this;
        }

        public CreatePendingServiceRequest.Builder durationType(DurationType durationType) {
            this.durationType = durationType;
            return this;
        }

        public CreatePendingServiceRequest.Builder fixedDurationInSeconds(Integer fixedDurationInSeconds) {
            this.fixedDurationInSeconds = fixedDurationInSeconds;
            return this;
        }

        public CreatePendingServiceRequest.Builder minDurationInSeconds(Integer minDurationInSeconds) {
            this.minDurationInSeconds = minDurationInSeconds;
            return this;
        }

        public CreatePendingServiceRequest.Builder maxDurationInSeconds(Integer maxDurationInSeconds) {
            this.maxDurationInSeconds = maxDurationInSeconds;
            return this;
        }

        public CreatePendingServiceRequest.Builder reservationDeadlineDays(Integer reservationDeadlineDays) {
            this.reservationDeadlineDays = reservationDeadlineDays;
            return this;
        }

        public CreatePendingServiceRequest.Builder cancellationDeadlineDays(Integer cancellationDeadlineDays) {
            this.cancellationDeadlineDays = cancellationDeadlineDays;
            return this;
        }

        public CreatePendingServiceRequest.Builder reservationType(ReservationType reservationType) {
            this.reservationType = reservationType;
            return this;
        }

        public CreatePendingServiceRequest.Builder categoryName(String categoryName) {
            this.categoryName = categoryName;
            return this;
        }

        public CreatePendingServiceRequest.Builder businessOwnerId(Long businessOwnerId) {
            this.businessOwnerId = businessOwnerId;
            return this;
        }

        public CreatePendingServiceRequest.Builder eventTypeIds(Collection<Long> eventTypeIds) {
            this.eventTypeIds = eventTypeIds;
            return this;
        }

        public CreatePendingServiceRequest build() {
            return new CreatePendingServiceRequest(this);
        }

    }

    public static CreatePendingServiceRequest.Builder builder() {
        return new CreatePendingServiceRequest.Builder();
    }
}
