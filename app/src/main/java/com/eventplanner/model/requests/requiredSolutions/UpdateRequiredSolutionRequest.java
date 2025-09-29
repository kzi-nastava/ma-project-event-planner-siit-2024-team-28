package com.eventplanner.model.requests.requiredSolutions;

public class UpdateRequiredSolutionRequest {
    private Double budget;
    private Long solutionId;

    public UpdateRequiredSolutionRequest(Double budget, Long solutionId) {
        this.budget = budget;
        this.solutionId = solutionId;
    }

    public void setBudget(Double budget) {
        this.budget = budget;
    }

    public void setSolutionId(Long solutionId) {
        this.solutionId = solutionId;
    }

}
