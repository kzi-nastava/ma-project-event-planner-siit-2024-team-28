package com.eventplanner.services;

import com.eventplanner.model.requests.reports.CreateReportRequest;
import com.eventplanner.model.responses.reports.GetReportResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ReportService {
    @POST("reports")
    Call<GetReportResponse> createReport(@Body CreateReportRequest request);

}
