package com.eventplanner.model.responses.requiredSolutions;

public class GetRequiredSolutionItemResponse {
    private Long id;
    private Double budget;
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
