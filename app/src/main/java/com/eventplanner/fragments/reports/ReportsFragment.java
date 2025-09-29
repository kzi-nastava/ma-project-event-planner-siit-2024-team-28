package com.eventplanner.fragments.reports;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eventplanner.R;
import com.eventplanner.adapters.reports.ReportsAdapter;
import com.eventplanner.model.responses.PagedResponse;
import com.eventplanner.model.responses.reports.GetReportResponse;
import com.eventplanner.services.ReportService;
import com.eventplanner.utils.HttpUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportsFragment extends Fragment {

    private Spinner statusSpinner;
    private RecyclerView recyclerView;
    private ReportsAdapter adapter;
    private ProgressBar progressBar;
    private Button prevPageButton, nextPageButton;

    private List<GetReportResponse> reports = new ArrayList<>();
    private String selectedStatus = "ALL";
    private int page = 0;
    private int pageSize = 10;
    private int totalCount = 0;

    private final String[] statuses = {"ALL", "PENDING", "ACCEPTED", "REJECTED"};

    private ReportService reportService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        HttpUtils.initialize(getContext());

        return inflater.inflate(R.layout.fragment_reports, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        reportService = HttpUtils.getReportService();
        super.onViewCreated(view, savedInstanceState);

        statusSpinner = view.findViewById(R.id.status_spinner);
        recyclerView = view.findViewById(R.id.reports_recycler_view);
        progressBar = view.findViewById(R.id.progress_bar);
        prevPageButton = view.findViewById(R.id.prev_page_button);
        nextPageButton = view.findViewById(R.id.next_page_button);

        adapter = new ReportsAdapter(reports, this::onEditStatusClick, this::onDeleteClick, this::onUserClick);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, statuses);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(spinnerAdapter);

        statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedStatus = statuses[position];
                page = 0;
                getReports();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        prevPageButton.setOnClickListener(v -> {
            if (page > 0) {
                page--;
                getReports();
            }
        });

        nextPageButton.setOnClickListener(v -> {
            if ((page + 1) * pageSize < totalCount) {
                page++;
                getReports();
            }
        });

        getReports();
    }

    private void getReports() {
        progressBar.setVisibility(View.VISIBLE);
        Call<PagedResponse<GetReportResponse>> call;

        if ("ALL".equals(selectedStatus)) {
            call = reportService.getAllReports(page, pageSize);
        } else {
            call = reportService.getReportsByStatus(selectedStatus.toUpperCase(), page, pageSize);
        }

        call.enqueue(new Callback<PagedResponse<GetReportResponse>>() {
            @Override
            public void onResponse(Call<PagedResponse<GetReportResponse>> call, Response<PagedResponse<GetReportResponse>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    reports.clear();
                    reports.addAll(response.body().getContent());
                    totalCount = Math.toIntExact(response.body().getTotalElements());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(requireContext(), "Failed to get reports", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PagedResponse<GetReportResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onEditStatusClick(GetReportResponse report, String newStatus) {
        reportService.updateReportStatus(report.getId(), newStatus).enqueue(new Callback<GetReportResponse>() {
            @Override
            public void onResponse(Call<GetReportResponse> call, Response<GetReportResponse> response) {
                if (response.isSuccessful()) {
                    report.setStatus(newStatus);
                    report.setSuspended(response.body().getIsSuspended());
                    report.setSuspensionTimestamp(response.body().getSuspensionTimestamp());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(requireContext(), "Failed to update status", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GetReportResponse> call, Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onDeleteClick(GetReportResponse report) {
        reportService.deleteReportById(report.getId()).enqueue(new Callback<GetReportResponse>() {
            @Override
            public void onResponse(Call<GetReportResponse> call, Response<GetReportResponse> response) {
                if (response.isSuccessful()) {
                    reports.remove(report);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(requireContext(), "Report deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Failed to delete report", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GetReportResponse> call, Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onUserClick(long userId) {
        Bundle bundle = new Bundle();
        bundle.putString("businessOwnerId", String.valueOf(userId));
        Navigation.findNavController(requireView()).navigate(R.id.action_reportsFragment_to_business_owner_details_fragment, bundle);
    }
}
