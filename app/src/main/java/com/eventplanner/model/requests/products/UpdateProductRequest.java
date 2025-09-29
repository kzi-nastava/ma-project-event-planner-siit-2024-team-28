package com.eventplanner.model.requests.products;

import java.util.List;

public class UpdateProductRequest {
    private String name;
    private String description;
    private Double price;
    private Double discount;
    private List<String> imagesBase64;
    private Boolean isVisibleForEventOrganizers;
    private Boolean isAvailable;
    private List<Long> eventTypeIds;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UpdateProductRequest request = new UpdateProductRequest();

        public Builder name(String name) {
            request.name = name;
            return this;
        }

        public Builder description(String description) {
            request.description = description;
            return this;
        }

        public Builder price(Double price) {
            request.price = price;
            return this;
        }

        public Builder discount(Double discount) {
            request.discount = discount;
            return this;
        }

        public Builder imagesBase64(List<String> imagesBase64) {
            request.imagesBase64 = imagesBase64;
            return this;
        }

        public Builder isVisible(Boolean isVisible) {
            request.isVisibleForEventOrganizers = isVisible;
            return this;
        }

        public Builder isAvailable(Boolean isAvailable) {
            request.isAvailable = isAvailable;
            return this;
        }

        public Builder eventTypeIds(List<Long> eventTypeIds) {
            request.eventTypeIds = eventTypeIds;
            return this;
        }

        public UpdateProductRequest build() {
            return request;
        }
    }

    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Double getPrice() { return price; }
    public Double getDiscount() { return discount; }
    public List<String> getImagesBase64() { return imagesBase64; }
    public Boolean getIsVisibleForEventOrganizers() { return isVisibleForEventOrganizers; }
    public Boolean getIsAvailable() { return isAvailable; }
    public List<Long> getEventTypeIds() { return eventTypeIds; }
}