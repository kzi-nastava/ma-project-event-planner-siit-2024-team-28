package com.eventplanner.services;

import com.eventplanner.model.requests.solutionComments.CreateSolutionCommentRequest;
import com.eventplanner.model.responses.solutionComments.GetSolutionCommentPreviewResponse;
import com.eventplanner.model.responses.solutionComments.GetSolutionCommentResponse;

import java.util.Collection;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface SolutionCommentService {
    @POST("solutions/comments")
    Call<GetSolutionCommentResponse> createComment(@Body CreateSolutionCommentRequest request);
    @GET("solutions/comments/business-owner/{businessOwnerId}")
    Call<Collection<GetSolutionCommentPreviewResponse>> getAllCommentsByBusinessOwnerId(@Path("businessOwnerId") Long businessOwnerId);
}
