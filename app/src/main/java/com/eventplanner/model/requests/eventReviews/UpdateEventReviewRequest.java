package com.eventplanner.model.requests.eventReviews;

public class UpdateEventReviewRequest {
    private Short rating;

    public UpdateEventReviewRequest() {
    }

    public UpdateEventReviewRequest(Short rating) {
        this.rating = rating;
    }

    public Short getRating() {
        return rating;
    }

    public void setRating(Short rating) {
        this.rating = rating;
    }
}
