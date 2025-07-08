package com.eventplanner.model.responses.reviews;

public class GetReviewResponse {
    Long id;
    Short rating;
    Long solutionId;
    Long reviewerId;

    public Long getId() {
        return id;
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
}
