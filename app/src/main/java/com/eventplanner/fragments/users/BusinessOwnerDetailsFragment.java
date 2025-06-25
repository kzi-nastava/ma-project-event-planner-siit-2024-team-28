package com.eventplanner.fragments.users;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.eventplanner.R;
import com.eventplanner.adapters.comments.CommentListAdapter;
import com.eventplanner.databinding.FragmentBusinessOwnerDetailsBinding;
import com.eventplanner.databinding.FragmentSolutionDetailsBinding;
import com.eventplanner.model.requests.reports.CreateReportRequest;
import com.eventplanner.model.responses.comments.GetCommentPreviewResponse;
import com.eventplanner.model.responses.reports.GetReportResponse;
import com.eventplanner.model.responses.users.GetUserResponse;
import com.eventplanner.services.CommentService;
import com.eventplanner.services.ReportService;
import com.eventplanner.services.UserService;
import com.eventplanner.utils.HttpUtils;

import java.util.ArrayList;
import java.util.Collection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class BusinessOwnerDetailsFragment extends Fragment {

    private FragmentBusinessOwnerDetailsBinding binding;
    private static final String ARG_BUSINESS_OWNER_ID = "businessOwnerId";
    private UserService userService;
    private ReportService reportService;
    private CommentService commentService;
    private String businessOwnerId;
    private GetUserResponse businessOwner;

    public BusinessOwnerDetailsFragment() {
        // Required empty public constructor
    }

    public static BusinessOwnerDetailsFragment newInstance(String businessOwnerId) {
        BusinessOwnerDetailsFragment fragment = new BusinessOwnerDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_BUSINESS_OWNER_ID, businessOwnerId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userService = HttpUtils.getUserService();
        reportService = HttpUtils.getReportService();
        commentService = HttpUtils.getCommentService();
        if (getArguments() != null) {
            businessOwnerId = getArguments().getString(ARG_BUSINESS_OWNER_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentBusinessOwnerDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // getting business owner details from backend
        fetchBusinessOwnerDetails();
    }

    // function for fetching BusinessOwner data from backend
    private void fetchBusinessOwnerDetails() {
        Call<GetUserResponse> call = userService.getUserById(Long.parseLong(businessOwnerId));
        call.enqueue(new Callback<GetUserResponse>() {
            @Override
            public void onResponse(Call<GetUserResponse> call, Response<GetUserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    businessOwner = response.body();
                    populateViews();
                    fetchComments(Long.parseLong(businessOwnerId));
                    Log.i("BusinessOwnerDetailsFragment", "Business owner successfully fetched with id: " + businessOwnerId);
                } else {
                    Log.w("BusinessOwnerDetailsFragment", "Error with fetching business owner: " + response.code());
                    showErrorDialog();

                }
            }

            @Override
            public void onFailure(Call<GetUserResponse> call, Throwable t) {
                Log.e("BusinessOwnerDetailsFragment", "Network failure", t);
                showErrorDialog();
            }
        });
    }

    // code for editing views separated from main code
    private void populateViews() {
        binding.textBusinessName.setText(businessOwner.getBusinessName());
        binding.textAddress.setText(binding.textAddress.getText() + " " + businessOwner.getAddress());
        binding.textDescription.setText(binding.textDescription.getText() + " " + businessOwner.getBusinessDescription());
        binding.textPhoneNumber.setText(binding.textPhoneNumber.getText() + " " + businessOwner.getPhoneNumber());

        // on click opens dialog
        binding.reportUserButton.setOnClickListener(v -> {
            Log.i("BusinessOwnerFragment", "Attempt creating report.");
            showReportDialog();
        });
    }

    // function for making dialog -> SUBMIT: call function for making request to back | CANCEL: nothing happens
    private void showReportDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Report User");

        final EditText input = new EditText(requireContext());
        input.setHint("Reason");
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setMinLines(3);
        input.setPadding(40, 30, 40, 30);

        builder.setView(input);

        builder.setPositiveButton("Submit", (dialog, which) -> {
            String reportText = input.getText().toString().trim();
            if (!reportText.isEmpty()) {
                createReport(reportText);
            } else {
                Toast.makeText(requireContext(), "Reason is required.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // code for making request to backend for making report
    private void createReport(String reportText) {
        CreateReportRequest request = new CreateReportRequest(
                Long.parseLong(businessOwnerId),
                reportText);

        Call<GetReportResponse> call = reportService.createReport(request);
        call.enqueue(new Callback<GetReportResponse>() {
            @Override
            public void onResponse(Call<GetReportResponse> call, Response<GetReportResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Report sent successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Failed to send report", Toast.LENGTH_SHORT).show();
                    Log.e("BusinessOwnerDetailsFragment", "Error while creating report: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GetReportResponse> call, Throwable t) {
                Log.e("BusinessOwnerDetailsFragment", "Network failure", t);
            }
        });
    }

    private void fetchComments(Long businessOwnerId) {
        Call<Collection<GetCommentPreviewResponse>> call = commentService.getAllCommentsByBusinessOwnerById(businessOwnerId);

        call.enqueue(new Callback<Collection<GetCommentPreviewResponse>>() {
            @Override
            public void onResponse(Call<Collection<GetCommentPreviewResponse>> call, Response<Collection<GetCommentPreviewResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Collection<GetCommentPreviewResponse> comments = response.body();
                    Log.i("BusinessOwnerDetailsFragment", "Comments for business owner with id:" + businessOwnerId + " successfully fetched");
                    CommentListAdapter adapter = new CommentListAdapter(getContext(), new ArrayList<>(comments));
                    binding.commentsListView.setAdapter(adapter);

                } else {
                    Log.e("BusinessOwnerDetailsFragment", "Error response code: " + response.code());
                    showErrorDialog();
                }
            }

            @Override
            public void onFailure(Call<Collection<GetCommentPreviewResponse>> call, Throwable t) {
                Log.e("BusinessOwnerDetailsFragment", "Network error", t);
                showErrorDialog();
            }
        });
    }

    private void showErrorDialog() {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setMessage("An error has occured.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }


}