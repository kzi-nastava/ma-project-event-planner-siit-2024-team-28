package com.eventplanner.model.responses.products;

import java.util.List;

public class GetProductResponse {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Double discount;
    private List<String> imagesBase64;
    private Boolean isVisibleForEventOrganizers;
    private Boolean isAvailable;
    private Boolean isDeleted;
    private Long categoryId;
    private String categoryName;
    private Long businessOwnerId;
    private List<Long> eventTypeIds;

    // Constructors
    public GetProductResponse() {}

    public GetProductResponse(Long id, String name, String description, Double price, Double discount,
                              List<String> imagesBase64, Boolean isVisibleForEventOrganizers, Boolean isAvailable,
                              Boolean isDeleted, Long categoryId, String categoryName, Long businessOwnerId,
                              List<Long> eventTypeIds) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.discount = discount;
        this.imagesBase64 = imagesBase64;
        this.isVisibleForEventOrganizers = isVisibleForEventOrganizers;
        this.isAvailable = isAvailable;
        this.isDeleted = isDeleted;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.businessOwnerId = businessOwnerId;
        this.eventTypeIds = eventTypeIds;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Double getDiscount() { return discount; }
    public void setDiscount(Double discount) { this.discount = discount; }

    public List<String> getImagesBase64() { return imagesBase64; }
    public void setImagesBase64(List<String> imagesBase64) { this.imagesBase64 = imagesBase64; }

    public Boolean getIsVisibleForEventOrganizers() { return isVisibleForEventOrganizers; }
    public void setIsVisibleForEventOrganizers(Boolean isVisibleForEventOrganizers) { 
        this.isVisibleForEventOrganizers = isVisibleForEventOrganizers; 
    }

    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }

    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public Long getBusinessOwnerId() { return businessOwnerId; }
    public void setBusinessOwnerId(Long businessOwnerId) { this.businessOwnerId = businessOwnerId; }

    public List<Long> getEventTypeIds() { return eventTypeIds; }
    public void setEventTypeIds(List<Long> eventTypeIds) { this.eventTypeIds = eventTypeIds; }
}
