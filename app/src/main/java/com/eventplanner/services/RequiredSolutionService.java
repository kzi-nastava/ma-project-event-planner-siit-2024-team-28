package com.eventplanner.services;

import com.eventplanner.model.requests.requiredSolutions.CreateRequiredSolutionRequest;
import com.eventplanner.model.responses.requiredSolutions.GetRequiredSolutionItemResponse;

import java.util.Collection;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RequiredSolutionService {
    @POST("required-solutions")
    Call<Long> createRequiredSolution(@Body CreateRequiredSolutionRequest requiredSolution);

    @GET("required-solutions/event/{eventId}")
    Call<Collection<GetRequiredSolutionItemResponse>> getRequiredSolutionsForEvent(@Path("eventId") Long eventId);

    @DELETE("required-solutions/{id}")
    Call<Void> deleteRequiredSolution(@Path("id") Long id);
}
