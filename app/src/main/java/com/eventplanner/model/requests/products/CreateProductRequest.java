package com.eventplanner.model.requests.products;

import java.util.List;

public class CreateProductRequest {
    private String name;
    private String description;
    private Double price;
    private Double discount;
    private List<String> imagesBase64;
    private Boolean isVisible;
    private Boolean isAvailable;
    private Long solutionCategoryId;
    private Long businessOwnerId;
    private List<Long> eventTypeIds;

    public CreateProductRequest() {
    }

    public CreateProductRequest(String name, String description, Double price, Double discount, List<String> imagesBase64, Boolean isVisible, Boolean isAvailable, Long solutionCategoryId, Long businessOwnerId, List<Long> eventTypeIds) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.discount = discount;
        this.imagesBase64 = imagesBase64;
        this.isVisible = isVisible;
        this.isAvailable = isAvailable;
        this.solutionCategoryId = solutionCategoryId;
        this.businessOwnerId = businessOwnerId;
        this.eventTypeIds = eventTypeIds;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public List<String> getImagesBase64() {
        return imagesBase64;
    }

    public void setImagesBase64(List<String> imagesBase64) {
        this.imagesBase64 = imagesBase64;
    }

    public Boolean getVisible() {
        return isVisible;
    }

    public void setVisible(Boolean visible) {
        isVisible = visible;
    }

    public Boolean getAvailable() {
        return isAvailable;
    }

    public void setAvailable(Boolean available) {
        isAvailable = available;
    }

    public Long getSolutionCategoryId() {
        return solutionCategoryId;
    }

    public void setSolutionCategoryId(Long solutionCategoryId) {
        this.solutionCategoryId = solutionCategoryId;
    }

    public Long getBusinessOwnerId() {
        return businessOwnerId;
    }

    public void setBusinessOwnerId(Long businessOwnerId) {
        this.businessOwnerId = businessOwnerId;
    }

    public List<Long> getEventTypeIds() {
        return eventTypeIds;
    }

    public void setEventTypeIds(List<Long> eventTypeIds) {
        this.eventTypeIds = eventTypeIds;
    }
}