package com.eventplanner.fragments.solutionComments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eventplanner.R;
import com.eventplanner.adapters.solutionComments.SolutionCommentsModerationAdapter;
import com.eventplanner.model.enums.RequestStatus;
import com.eventplanner.model.responses.PagedResponse;
import com.eventplanner.model.responses.solutionComments.GetSolutionCommentResponse;
import com.eventplanner.services.SolutionCommentService;
import com.eventplanner.utils.HttpUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SolutionCommentsModerationFragment extends Fragment
        implements SolutionCommentsModerationAdapter.CommentActionListener {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private Spinner statusFilterSpinner;
    private SolutionCommentsModerationAdapter adapter;
    private List<GetSolutionCommentResponse> comments = new ArrayList<>();

    private SolutionCommentService commentService;

    private int currentPage = 0;
    private final int pageSize = 20;
    private RequestStatus currentFilterStatus = null; // null means "all statuses"

    public SolutionCommentsModerationFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_comments_moderation, container, false);

        recyclerView = root.findViewById(R.id.recycler_view_comments);
        progressBar = root.findViewById(R.id.loading_indicator);
        statusFilterSpinner = root.findViewById(R.id.status_filter_spinner);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SolutionCommentsModerationAdapter(comments, this);
        recyclerView.setAdapter(adapter);

        // Setup Spinner with status options
        setupStatusFilterSpinner();

        commentService = HttpUtils.getCommentService();

        // Load initial comments
        loadComments();

        return root;
    }

    private void setupStatusFilterSpinner() {
        // Create an array of status options including "All"
        String[] statusOptions = new String[RequestStatus.values().length + 1];
        statusOptions[0] = "ALL COMMENTS";

        for (int i = 0; i < RequestStatus.values().length; i++) {
            statusOptions[i + 1] = RequestStatus.values()[i].name();
        }

        // Create adapter for spinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                statusOptions
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusFilterSpinner.setAdapter(spinnerAdapter);

        // Set up spinner selection listener
        statusFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    currentFilterStatus = null; // Show all statuses
                } else {
                    currentFilterStatus = RequestStatus.values()[position - 1];
                }
                // Reset page and reload comments with new filter
                currentPage = 0;
                loadComments();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void loadComments() {
        progressBar.setVisibility(View.VISIBLE);

        Call<PagedResponse<GetSolutionCommentResponse>> call;

        if (currentFilterStatus == null) {
            // Load all comments without status filter
            call = commentService.getAllComments(currentPage, pageSize);
        } else {
            // Load comments filtered by status
            call = commentService.getCommentsByStatus(currentPage, pageSize, currentFilterStatus);
        }

        call.enqueue(new Callback<PagedResponse<GetSolutionCommentResponse>>() {
            @Override
            public void onResponse(@NonNull Call<PagedResponse<GetSolutionCommentResponse>> call,
                                   @NonNull Response<PagedResponse<GetSolutionCommentResponse>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    // If it's the first page, clear existing comments
                    if (currentPage == 0) {
                        comments.clear();
                    }

                    comments.addAll(response.body().getContent());
                    adapter.notifyDataSetChanged();

                } else {
                    Toast.makeText(getContext(), "Failed to load comments", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PagedResponse<GetSolutionCommentResponse>> call,
                                  @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onStatusChange(GetSolutionCommentResponse comment, RequestStatus newStatus) {
        progressBar.setVisibility(View.VISIBLE);

        commentService.updateCommentStatus(comment.getId(), newStatus)
                .enqueue(new Callback<GetSolutionCommentResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<GetSolutionCommentResponse> call,
                                           @NonNull Response<GetSolutionCommentResponse> response) {
                        progressBar.setVisibility(View.GONE);

                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(getContext(), "Status updated", Toast.LENGTH_SHORT).show();
                            // Reload comments to reflect the change
                            loadComments();
                        } else {
                            Toast.makeText(getContext(), "Failed to update status", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<GetSolutionCommentResponse> call,
                                          @NonNull Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onDelete(GetSolutionCommentResponse comment) {
        progressBar.setVisibility(View.VISIBLE);

        commentService.deleteComment(comment.getId())
                .enqueue(new Callback<GetSolutionCommentResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<GetSolutionCommentResponse> call,
                                           @NonNull Response<GetSolutionCommentResponse> response) {
                        progressBar.setVisibility(View.GONE);

                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Comment deleted", Toast.LENGTH_SHORT).show();
                            // Reload comments to reflect the change
                            loadComments();
                        } else {
                            Toast.makeText(getContext(), "Failed to delete", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<GetSolutionCommentResponse> call,
                                          @NonNull Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onGoToSolution(long solutionId) {
        Bundle bundle = new Bundle();
        bundle.putString("solutionId", String.valueOf(solutionId));
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_nav_comments_moderation_to_nav_solution_details, bundle);
    }

    @Override
    public void onGoToUser(long userId) {
        Bundle bundle = new Bundle();
        bundle.putString("businessOwnerId", String.valueOf(userId));
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_nav_comments_moderation_to_business_owner_details_fragment, bundle);
    }
}