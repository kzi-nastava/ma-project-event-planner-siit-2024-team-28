package com.eventplanner.model.responses.products;

import java.util.List;

public class GetProductHistoryResponse {
    private Long id;
    private Long productId;
    private String name;
    private String description;
    private Double price;
    private Double discount;
    private List<String> imagesBase64;
    private Boolean isVisibleForEventOrganizers;
    private Boolean isAvailable;
    private Long categoryId;
    private String categoryName;
    private List<Long> eventTypeIds;
    private String changeDate;
    private String changeReason;
    private Long changedByUserId;

    // Constructors
    public GetProductHistoryResponse() {}

    public GetProductHistoryResponse(Long id, Long productId, String name, String description,
                                     Double price, Double discount, List<String> imagesBase64,
                                     Boolean isVisibleForEventOrganizers, Boolean isAvailable,
                                     Long categoryId, String categoryName, List<Long> eventTypeIds,
                                     String changeDate, String changeReason, Long changedByUserId) {
        this.id = id;
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.discount = discount;
        this.imagesBase64 = imagesBase64;
        this.isVisibleForEventOrganizers = isVisibleForEventOrganizers;
        this.isAvailable = isAvailable;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.eventTypeIds = eventTypeIds;
        this.changeDate = changeDate;
        this.changeReason = changeReason;
        this.changedByUserId = changedByUserId;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

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

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public List<Long> getEventTypeIds() { return eventTypeIds; }
    public void setEventTypeIds(List<Long> eventTypeIds) { this.eventTypeIds = eventTypeIds; }

    public String getChangeDate() { return changeDate; }
    public void setChangeDate(String changeDate) { this.changeDate = changeDate; }

    public String getChangeReason() { return changeReason; }
    public void setChangeReason(String changeReason) { this.changeReason = changeReason; }

    public Long getChangedByUserId() { return changedByUserId; }
    public void setChangedByUserId(Long changedByUserId) { this.changedByUserId = changedByUserId; }
}
