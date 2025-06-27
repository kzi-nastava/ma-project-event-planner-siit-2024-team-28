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

    @GET("services/business-owner/{businessOwnerId}/filter")
    Call<PagedResponse<GetServiceResponse>> filterServicesByBusinessOwner(
            @Path("businessOwnerId") Long businessOwnerId,
            @Query("name") String name,
            @Query("categoryId") Long categoryId,
            @Query("eventTypeId") Long eventTypeId,
            @Query("minPrice") Double minPrice,
            @Query("maxPrice") Double maxPrice,
            @Query("isAvailable") Boolean isAvailable,
            @Query("isVisible") Boolean isVisible,
            @Query("isDeleted") Boolean isDeleted,
            @Query("page") int page,
            @Query("size") int size
    );
}
