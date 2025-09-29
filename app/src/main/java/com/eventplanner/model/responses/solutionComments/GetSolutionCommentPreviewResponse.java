package com.eventplanner.model.responses.solutionComments;

public class GetSolutionCommentPreviewResponse {
    private Long id;
    private String content;
    private String solutionName;
    private String commenterName;

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public String getSolutionName() {
        return solutionName;
    }

    public String getCommenterName() {
        return commenterName;
    }
}
