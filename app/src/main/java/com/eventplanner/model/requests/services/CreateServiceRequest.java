package com.eventplanner.model.requests.services;

import com.eventplanner.model.enums.ReservationType;
import com.eventplanner.model.enums.SolutionStatus;

import java.util.Collection;

public class CreateServiceRequest {
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
    private Long businessOwnerId;
    private SolutionStatus status;
    private Collection<Long> eventTypeIds;

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    private CreateServiceRequest(Builder builder) {
        this.name = builder.name;
        this.description = builder.description;
        this.price = builder.price;
        this.discount = builder.discount;
        this.imageBase64 = builder.imageBase64;
        this.isDeleted = builder.isDeleted;
        this.isVisibleForEventOrganizers = builder.isVisibleForEventOrganizers;
        this.isAvailable = builder.isAvailable;
        this.specifics = builder.specifics;
        this.fixedDurationInSeconds = builder.fixedDurationInSeconds;
        this.minDurationInSeconds = builder.minDurationInSeconds;
        this.maxDurationInSeconds = builder.maxDurationInSeconds;
        this.reservationDeadlineDays = builder.reservationDeadlineDays;
        this.cancellationDeadlineDays = builder.cancellationDeadlineDays;
        this.reservationType = builder.reservationType;
        this.categoryId = builder.categoryId;
        this.businessOwnerId = builder.businessOwnerId;
        this.eventTypeIds = builder.eventTypeIds;
        this.status = builder.status;
    }

    // Static Builder class
    public static class Builder {
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
        private Long businessOwnerId;
        private SolutionStatus status;
        private Collection<Long> eventTypeIds;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder price(Double price) {
            this.price = price;
            return this;
        }

        public Builder discount(Double discount) {
            this.discount = discount;
            return this;
        }

        public Builder imageBase64(String imageBase64) {
            this.imageBase64 = imageBase64;
            return this;
        }

        public Builder isDeleted(Boolean isDeleted) {
            this.isDeleted = isDeleted;
            return this;
        }

        public Builder isVisibleForEventOrganizers(Boolean isVisibleForEventOrganizers) {
            this.isVisibleForEventOrganizers = isVisibleForEventOrganizers;
            return this;
        }

        public Builder isAvailable(Boolean isAvailable) {
            this.isAvailable = isAvailable;
            return this;
        }

        public Builder specifics(String specifics) {
            this.specifics = specifics;
            return this;
        }

        public Builder fixedDurationInSeconds(Integer fixedDurationInSeconds) {
            this.fixedDurationInSeconds = fixedDurationInSeconds;
            return this;
        }

        public Builder minDurationInSeconds(Integer minDurationInSeconds) {
            this.minDurationInSeconds = minDurationInSeconds;
            return this;
        }

        public Builder maxDurationInSeconds(Integer maxDurationInSeconds) {
            this.maxDurationInSeconds = maxDurationInSeconds;
            return this;
        }

        public Builder reservationDeadlineDays(Integer reservationDeadlineDays) {
            this.reservationDeadlineDays = reservationDeadlineDays;
            return this;
        }

        public Builder cancellationDeadlineDays(Integer cancellationDeadlineDays) {
            this.cancellationDeadlineDays = cancellationDeadlineDays;
            return this;
        }

        public Builder reservationType(ReservationType reservationType) {
            this.reservationType = reservationType;
            return this;
        }

        public Builder categoryId(Long categoryId) {
            this.categoryId = categoryId;
            return this;
        }

        public Builder businessOwnerId(Long businessOwnerId) {
            this.businessOwnerId = businessOwnerId;
            return this;
        }

        public Builder status(SolutionStatus status) {
            this.status = status;
            return this;
        }

        public Builder eventTypeIds(Collection<Long> eventTypeIds) {
            this.eventTypeIds = eventTypeIds;
            return this;
        }

        public CreateServiceRequest build() {
            return new CreateServiceRequest(this);
        }

    }

    public static Builder builder() {
        return new Builder();
    }

}
