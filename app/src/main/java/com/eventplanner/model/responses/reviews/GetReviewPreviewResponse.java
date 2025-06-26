package com.eventplanner.model.responses.reviews;

public class GetReviewPreviewResponse {
    private Long id;
    private Short rating;
    private String solutionName;
    private String reviewerName;

    public Long getId() {
        return id;
    }

    public Short getRating() {
        return rating;
    }

    public String getSolutionName() {
        return solutionName;
    }

    public String getReviewerName() {
        return reviewerName;
    }
}
