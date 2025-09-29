package com.eventplanner.services;

import com.eventplanner.model.requests.reports.CreateReportRequest;
import com.eventplanner.model.responses.PagedResponse;
import com.eventplanner.model.responses.reports.GetReportResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ReportService {
    @POST("reports")
    Call<GetReportResponse> createReport(@Body CreateReportRequest request);

    @GET("reports")
    Call<PagedResponse<GetReportResponse>> getAllReports(@Query("page") int page,
                                                         @Query("size") int size);

    @GET("reports/status")
    Call<PagedResponse<GetReportResponse>> getReportsByStatus(@Query("status") String status,
                                                              @Query("page") int page,
                                                              @Query("size") int size);

    @GET("reports/{id}")
    Call<GetReportResponse> getReportById(@Path("id") Long id);

    @PATCH("reports/{id}/status")
    Call<GetReportResponse> updateReportStatus(@Path("id") Long id, @Query("status") String status);

    @DELETE("reports/{id}")
    Call<GetReportResponse> deleteReportById(@Path("id") Long id);
}
