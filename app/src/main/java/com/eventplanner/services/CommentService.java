package com.eventplanner.services;

import com.eventplanner.model.responses.comments.GetCommentPreviewResponse;

import java.util.Collection;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface CommentService {
    @GET("comments/business-owner/{id}")
    Call<Collection<GetCommentPreviewResponse>> getAllCommentsByBusinessOwnerById(@Path("id") Long id);
}
