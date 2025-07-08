package com.eventplanner.model.requests.comments;

public class CreateCommentRequest {
    private String content;
    private Long commenterId;
    private Long solutionId;

    private CreateCommentRequest(Builder builder) {
        this.content = builder.content;
        this.commenterId = builder.commenterId;
        this.solutionId = builder.solutionId;
    }

    public String getContent() {
        return content;
    }

    public Long getCommenterId() {
        return commenterId;
    }

    public Long getSolutionId() {
        return solutionId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String content;
        private Long commenterId;
        private Long solutionId;

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder commenterId(Long commenterId) {
            this.commenterId = commenterId;
            return this;
        }

        public Builder solutionId(Long solutionId) {
            this.solutionId = solutionId;
            return this;
        }

        public CreateCommentRequest build() {
            return new CreateCommentRequest(this);
        }
    }
}
