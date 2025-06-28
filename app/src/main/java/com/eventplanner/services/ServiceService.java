package com.eventplanner.services;

import com.eventplanner.model.requests.services.CreateServiceRequest;
import com.eventplanner.model.requests.services.UpdateServiceRequest;
import com.eventplanner.model.responses.PagedResponse;
import com.eventplanner.model.responses.services.GetServiceResponse;

import java.util.Collection;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ServiceService {
    @POST("services")
    Call<Long> createService(@Body CreateServiceRequest request);

    @GET("services/{id}")
    Call<GetServiceResponse> getServiceById(@Path("id") Long id);

    @GET("services/business-owner/{businessOwnerId}")
    Call<PagedResponse<GetServiceResponse>> getServicesByBusinessOwnerId(
            @Path("businessOwnerId") Long businessOwnerId,
            @Query("page") int page,
            @Query("size") int size
    );

    @PUT("services/{id}")
    Call<Void> updateService(@Path("id") Long id, @Body UpdateServiceRequest request);

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
