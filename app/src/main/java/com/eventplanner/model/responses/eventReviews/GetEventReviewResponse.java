package com.eventplanner.model.responses.eventReviews;

public class GetEventReviewResponse {
    private Long id;
    private Short rating;
    private Long eventId;
    private Long reviewerId;

    public GetEventReviewResponse() {
    }

    public GetEventReviewResponse(Long id, Short rating, Long eventId, Long reviewerId) {
        this.id = id;
        this.rating = rating;
        this.eventId = eventId;
        this.reviewerId = reviewerId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }
}
