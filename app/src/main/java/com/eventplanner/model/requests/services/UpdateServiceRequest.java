package com.eventplanner.model.requests.services;

import com.eventplanner.model.enums.DurationType;
import com.eventplanner.model.enums.ReservationType;

import java.util.Collection;
import java.util.List;

public class UpdateServiceRequest {
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
    private Collection<Long> eventTypeIds;

    private UpdateServiceRequest(Builder builder) {
        this.name = builder.name;
        this.description = builder.description;
        this.price = builder.price;
        this.discount = builder.discount;
        this.imageBase64 = builder.imageBase64;
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
        this.eventTypeIds = builder.eventTypeIds;
    }

    public static Builder builder() {
        return new Builder();
    }

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

        public Builder imageBase64(List<String> imageBase64) {
            this.imageBase64 = imageBase64;
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

        public Builder durationType(DurationType durationType) {
            this.durationType = durationType;
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

        public Builder eventTypeIds(Collection<Long> eventTypeIds) {
            this.eventTypeIds = eventTypeIds;
            return this;
        }

        public UpdateServiceRequest build() {
            return new UpdateServiceRequest(this);
        }
    }
}
