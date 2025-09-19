package com.eventplanner.services;

import com.eventplanner.model.responses.solutionHistories.GetSolutionHistoryDetails;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface SolutionHistoryService {
    @GET("solution-histories/solution-history-details/{id}")
    Call<GetSolutionHistoryDetails> getSolutionHistoryDetailsById(@Path("id") Long id);
}
