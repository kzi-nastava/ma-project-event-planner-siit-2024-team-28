package com.eventplanner.model.responses.requiredSolutions;

public class GetRequiredSolutionItemResponse {
    private Long id;
    private Double budget;

    public void setBudget(Double budget) {
        this.budget = budget;
    }

    public void setSolutionId(Long solutionId) {
        this.solutionId = solutionId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    private Long solutionId;
    private Long categoryId;
    private String categoryName;
    private Long eventId;

    // Getters
    public Long getId() {
        return id;
    }

    public Double getBudget() {
        return budget;
    }

    public Long getSolutionId() {
        return solutionId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public Long getEventId() {
        return eventId;
    }
}
