package com.eventplanner.model.responses.solutions;

public class GetPriceListSolutionResponse {
    private Long solutionId;
    private String name;
    private Double price;
    private Double discount;
    private Double finalPrice;


    public void setPrice(Double price) {
        this.price = price;
    }

    public void setFinalPrice(Double finalPrice) {
        this.finalPrice = finalPrice;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public Long getSolutionId() {
        return solutionId;
    }

    public String getName() {
        return name;
    }

    public Double getPrice() {
        return price;
    }

    public Double getDiscount() {
        return discount;
    }

    public Double getFinalPrice() {
        return finalPrice;
    }
}
