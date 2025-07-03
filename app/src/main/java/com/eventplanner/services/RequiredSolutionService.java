package com.eventplanner.services;

import com.eventplanner.model.requests.requiredSolutions.CreateRequiredSolutionRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RequiredSolutionService {
    @POST("required-solutions")
    Call<Long> createRequiredSolution(@Body CreateRequiredSolutionRequest requiredSolution);
}
