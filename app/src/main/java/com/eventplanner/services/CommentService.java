package com.eventplanner.services;

import com.eventplanner.model.requests.comments.CreateCommentRequest;
import com.eventplanner.model.responses.comments.GetCommentPreviewResponse;
import com.eventplanner.model.responses.comments.GetCommentResponse;

import java.util.Collection;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface CommentService {
    @POST("comments")
    Call<GetCommentResponse> createComment(@Body CreateCommentRequest request);
    @GET("comments/business-owner/{businessOwnerId}")
    Call<Collection<GetCommentPreviewResponse>> getAllCommentsByBusinessOwnerId(@Path("businessOwnerId") Long businessOwnerId);
}
