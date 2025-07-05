package com.eventplanner.model.requests.requiredSolutions;

public class UpdateRequiredSolutionRequest {
    private Double budget;
    private Long solutionId;
    private Long categoryId;
    private Long eventId;

    public UpdateRequiredSolutionRequest(Double budget, Long solutionId, Long categoryId, Long eventId) {
        this.budget = budget;
        this.solutionId = solutionId;
        this.categoryId = categoryId;
        this.eventId = eventId;
    }

    public void setBudget(Double budget) {
        this.budget = budget;
    }

    public void setSolutionId(Long solutionId) {
        this.solutionId = solutionId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }
}
