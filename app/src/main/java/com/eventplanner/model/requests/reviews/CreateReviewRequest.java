package com.eventplanner.model.requests.reviews;

public class CreateReviewRequest {
    Short rating;
    Long solutionId;
    Long reviewerId;

    private CreateReviewRequest(Builder builder) {
        this.rating = builder.rating;
        this.solutionId = builder.solutionId;
        this.reviewerId = builder.reviewerId;
    }

    public Short getRating() {
        return rating;
    }

    public Long getSolutionId() {
        return solutionId;
    }

    public Long getReviewerId() {
        return reviewerId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Short rating;
        private Long solutionId;
        private Long reviewerId;

        public Builder rating(Short rating) {
            this.rating = rating;
            return this;
        }

        public Builder solutionId(Long solutionId) {
            this.solutionId = solutionId;
            return this;
        }

        public Builder reviewerId(Long reviewerId) {
            this.reviewerId = reviewerId;
            return this;
        }

        public CreateReviewRequest build() {
            return new CreateReviewRequest(this);
        }
    }
}
