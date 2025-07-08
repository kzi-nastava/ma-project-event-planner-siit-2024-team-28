package com.eventplanner.model.responses.comments;

import com.eventplanner.model.enums.RequestStatus;

public class GetCommentResponse {
    Long id;
    String content;
    Long solutionId;
    Long commenterId;
    RequestStatus status;

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public Long getSolutionId() {
        return solutionId;
    }

    public Long getCommenterId() {
        return commenterId;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    Boolean isDeleted;
}
