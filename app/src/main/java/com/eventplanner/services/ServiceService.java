package com.eventplanner.services;

import com.eventplanner.model.requests.services.CreatePendingServiceRequest;
import com.eventplanner.model.requests.services.CreateServiceRequest;
import com.eventplanner.model.requests.services.UpdateServiceRequest;
import com.eventplanner.model.responses.PagedResponse;
import com.eventplanner.model.responses.services.DeleteServiceResponse;
import com.eventplanner.model.responses.services.GetServiceResponse;

import java.util.Collection;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ServiceService {
    @POST("services")
    Call<Long> createService(@Body CreateServiceRequest request);

    @POST("services/create-pending-service")
    Call<Long> createPendingService(@Body CreatePendingServiceRequest request);

    @GET("services/{id}")
    Call<GetServiceResponse> getServiceById(@Path("id") Long id);

    @GET("services/business-owner/{businessOwnerId}")
    Call<Collection<GetServiceResponse>> getServicesByBusinessOwnerId(
            @Path("businessOwnerId") Long businessOwnerId);

    @PUT("services/{id}")
    Call<Void> updateService(@Path("id") Long id, @Body UpdateServiceRequest request);

    @DELETE("services/{id}")
    Call<DeleteServiceResponse> deleteService(@Path("id") Long id);

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

    @GET("services/filter")
    Call<PagedResponse<GetServiceResponse>> filterServices(@Query("name") String name,
                                                           @Query("categoryId") Long categoryId,
                                                           @Query("eventTypeId") Long eventTypeId,
                                                           @Query("minPrice") Double minPrice,
                                                           @Query("maxPrice") Double maxPrice,
                                                           @Query("isAvailable") Boolean isAvailable,
                                                           @Query("page") int page,
                                                           @Query("size") int size
    );
}
