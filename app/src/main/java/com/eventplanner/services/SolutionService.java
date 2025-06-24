package com.eventplanner.services;

import com.eventplanner.model.responses.solutions.GetSolutionResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface SolutionService {
    @GET("solutions/{id}")
    Call<GetSolutionResponse> getSolutionById(@Path("id") Long id);
}
