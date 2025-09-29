package com.eventplanner.model.requests.requiredSolutions;

public class CreateRequiredSolutionRequest {
    private Double budget;
    private Long solutionId;
    private Long categoryId;
    private Long eventId;

    private CreateRequiredSolutionRequest(Builder builder) {
        this.budget = builder.budget;
        this.solutionId = builder.solutionId;
        this.categoryId = builder.categoryId;
        this.eventId = builder.eventId;
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

    public static class Builder {
        private Double budget;
        private Long solutionId;
        private Long categoryId;
        private Long eventId;

        public Builder budget(Double budget) {
            this.budget = budget;
            return this;
        }

        public Builder solutionId(Long solutionId) {
            this.solutionId = solutionId;
            return this;
        }

        public Builder categoryId(Long categoryId) {
            this.categoryId = categoryId;
            return this;
        }

        public Builder eventId(Long eventId) {
            this.eventId = eventId;
            return this;
        }

        public CreateRequiredSolutionRequest build() {
            return new CreateRequiredSolutionRequest(this);
        }
    }
}
