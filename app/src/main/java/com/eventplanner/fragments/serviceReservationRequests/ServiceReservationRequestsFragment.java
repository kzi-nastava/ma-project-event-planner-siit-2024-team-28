package com.eventplanner.fragments.serviceReservationRequests;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eventplanner.R;
import com.eventplanner.adapters.serviceReservationRequests.ServiceReservationRequestsAdapter;
import com.eventplanner.model.enums.RequestStatus;
import com.eventplanner.model.responses.PagedResponse;
import com.eventplanner.model.responses.serviceReservationRequests.GetServiceReservationRequestResponse;
import com.eventplanner.services.ServiceReservationRequestService;
import com.eventplanner.utils.AuthUtils;
import com.eventplanner.utils.HttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServiceReservationRequestsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ServiceReservationRequestsAdapter adapter;
    private Spinner statusFilter;
    private ServiceReservationRequestService serviceReservationRequestService;

    private Long businessOwnerId = 1L;
    private int currentPage = 0;
    private int pageSize = 10;

    private Button btnPrev, btnNext;

    public ServiceReservationRequestsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_service_reservation_requests, container, false);

        recyclerView = v.findViewById(R.id.recyclerReservations);
        statusFilter = v.findViewById(R.id.statusFilter);
        btnPrev = v.findViewById(R.id.btnPrev);
        btnNext = v.findViewById(R.id.btnNext);

        ArrayAdapter<CharSequence> filterAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.filter_statuses,
                android.R.layout.simple_spinner_item
        );
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusFilter.setAdapter(filterAdapter);

        businessOwnerId = AuthUtils.getUserId(getContext());
        serviceReservationRequestService = HttpUtils.getServiceReservationRequestService();

        adapter = new ServiceReservationRequestsAdapter(requireContext(), (req, newStatus) -> {
            Toast.makeText(requireContext(), "Processing reservation status update...", Toast.LENGTH_LONG).show();
            serviceReservationRequestService.updateServiceReservationRequest((long) req.getId(), newStatus)
                    .enqueue(new Callback<GetServiceReservationRequestResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<GetServiceReservationRequestResponse> call,
                                               @NonNull Response<GetServiceReservationRequestResponse> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(requireContext(), "Status updated!", Toast.LENGTH_SHORT).show();
                                loadReservations();
                            } else {
                                // Try to extract error message from response
                                String errorMessage = getString(R.string.failed_to_update_status); // fallback
                                try {
                                    if (response.errorBody() != null) {
                                        String errorBody = response.errorBody().string();
                                        // Parse JSON: {"error":"Invalid credentials"}
                                        JSONObject json = new JSONObject(errorBody);
                                        if (json.has("error")) {
                                            errorMessage = json.getString("error");
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(@NonNull Call<GetServiceReservationRequestResponse> call, @NonNull Throwable t) {
                            Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Filter listener
        statusFilter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                currentPage = 0; // reset to first page when filter changes
                loadReservations();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Pagination buttons
        btnPrev.setOnClickListener(v1 -> {
            if (currentPage > 0) {
                currentPage--;
                loadReservations();
            } else {
                Toast.makeText(requireContext(), "Already at first page", Toast.LENGTH_SHORT).show();
            }
        });

        btnNext.setOnClickListener(v1 -> {
            currentPage++;
            loadReservations();
        });

        // initial load
        loadReservations();

        return v;
    }

    private void loadReservations() {
        String selected = statusFilter.getSelectedItem().toString();

        Call<PagedResponse<GetServiceReservationRequestResponse>> call;
        if (selected.equals("ALL")) {
            call = serviceReservationRequestService.getServiceReservationRequestByBusinessOwnerId(
                    businessOwnerId, currentPage, pageSize
            );
        } else {
            RequestStatus status = RequestStatus.valueOf(selected);
            call = serviceReservationRequestService.getServiceReservationRequestByBusinessOwnerIdAndStatus(
                    businessOwnerId, status, currentPage, pageSize
            );
        }

        call.enqueue(new Callback<PagedResponse<GetServiceReservationRequestResponse>>() {
            @Override
            public void onResponse(@NonNull Call<PagedResponse<GetServiceReservationRequestResponse>> call,
                                   @NonNull Response<PagedResponse<GetServiceReservationRequestResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<GetServiceReservationRequestResponse> list = response.body().getContent();
                    Log.i("reserv", "Page " + currentPage + " -> " + list.toString());
                    adapter.setReservations(list);

                    // Disable Next if no more results
                    btnNext.setEnabled(!list.isEmpty() && list.size() == pageSize);
                    btnPrev.setEnabled(currentPage > 0);

                } else {
                    Toast.makeText(requireContext(), "Failed to load reservations", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PagedResponse<GetServiceReservationRequestResponse>> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
