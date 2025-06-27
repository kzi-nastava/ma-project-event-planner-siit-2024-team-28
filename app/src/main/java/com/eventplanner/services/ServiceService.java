package com.eventplanner.services;

import com.eventplanner.model.responses.PagedResponse;
import com.eventplanner.model.responses.services.GetServiceResponse;

import java.util.Collection;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ServiceService {
    @GET("services/business-owner/{businessOwnerId}")
    Call<PagedResponse<GetServiceResponse>> getServicesByBusinessOwnerId(
            @Path("businessOwnerId") Long businessOwnerId,
            @Query("page") int page,
            @Query("size") int size
    );
}
