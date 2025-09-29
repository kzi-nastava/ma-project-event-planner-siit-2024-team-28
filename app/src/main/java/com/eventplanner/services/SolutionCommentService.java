package com.eventplanner.services;

import com.eventplanner.model.enums.RequestStatus;
import com.eventplanner.model.requests.solutionComments.CreateSolutionCommentRequest;
import com.eventplanner.model.responses.PagedResponse;
import com.eventplanner.model.responses.solutionComments.GetSolutionCommentPreviewResponse;
import com.eventplanner.model.responses.solutionComments.GetSolutionCommentResponse;

import java.util.Collection;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SolutionCommentService {
    @POST("solutions/comments")
    Call<GetSolutionCommentResponse> createComment(@Body CreateSolutionCommentRequest request);
    @GET("solutions/comments/business-owner/{businessOwnerId}")
    Call<Collection<GetSolutionCommentPreviewResponse>> getAllCommentsByBusinessOwnerId(@Path("businessOwnerId") Long businessOwnerId);

    @GET("solutions/comments")
    Call<PagedResponse<GetSolutionCommentResponse>> getAllComments(@Query("page") int page,
                                                                   @Query("size") int size);

    @GET("solutions/comments/status")
    Call<PagedResponse<GetSolutionCommentResponse>> getCommentsByStatus(@Query("page") int page,
                                                                        @Query("size") int size,
                                                                        @Query("status")RequestStatus status);

    @PATCH("solutions/comments/{id}/status")
    Call<GetSolutionCommentResponse> updateCommentStatus(@Path("id") Long id,
                                                                        @Query("status") RequestStatus status);
    @DELETE("solutions/comments/{id}")
    Call<GetSolutionCommentResponse> deleteComment(@Path("id") Long id);
}

