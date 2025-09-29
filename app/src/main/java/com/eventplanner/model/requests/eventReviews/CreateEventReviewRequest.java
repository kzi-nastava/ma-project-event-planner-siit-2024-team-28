package com.eventplanner.model.requests.eventReviews;

public class CreateEventReviewRequest {
    private Short rating;
    private Long eventId;

    public CreateEventReviewRequest() {
    }

    public CreateEventReviewRequest(Short rating, Long eventId) {
        this.rating = rating;
        this.eventId = eventId;
    }

    public Short getRating() {
        return rating;
    }

    public void setRating(Short rating) {
        this.rating = rating;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }
}
