package com.eventplanner.services;

import com.eventplanner.model.enums.RequestStatus;
import com.eventplanner.model.requests.products.CreateProductRequest;
import com.eventplanner.model.requests.serviceReservationRequests.CreateServiceReservationRequestRequest;
import com.eventplanner.model.responses.PagedResponse;
import com.eventplanner.model.responses.serviceReservationRequests.GetServiceReservationRequestResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ServiceReservationRequestService {
    @POST("service-reservation-requests")
    Call<GetServiceReservationRequestResponse> createServiceReservationRequest(@Body CreateServiceReservationRequestRequest request);

    @GET("service-reservation-requests/business-owner/{id}")
    Call<PagedResponse<GetServiceReservationRequestResponse>> getServiceReservationRequestByBusinessOwnerId(@Path("id") Long businessOwnerId,
                                                                                                            @Query("page") int page,
                                                                                                            @Query("size") int size);

    @GET("service-reservation-requests/business-owner/{id}/status")
    Call<PagedResponse<GetServiceReservationRequestResponse>> getServiceReservationRequestByBusinessOwnerIdAndStatus(@Path("id") Long businessOwnerId,
                                                                                                                     @Query("status") RequestStatus status,
                                                                                                                     @Query("page") int page,
                                                                                                                     @Query("size") int size);
    @PATCH("service-reservation-requests/{id}/status")
    Call<GetServiceReservationRequestResponse> updateServiceReservationRequest(@Path("id") Long id, @Query("status") String status);

    @DELETE("service-reservation-requests/{id}")
    Call<Void> deleteServiceReservationRequest(@Path("id") Long id);
}
