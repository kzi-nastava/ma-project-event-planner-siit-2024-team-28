package com.eventplanner.model.responses.solutions;

import com.eventplanner.model.enums.FavoriteSolutionResult;

public class GetFavoriteSolutionResultResponse {
    private FavoriteSolutionResult resultType;
    private String resultMessage;

    public FavoriteSolutionResult getResultType() {
        return resultType;
    }

    public String getResultMessage() {
        return resultMessage;
    }
}
