package com.eventplanner.fragments.users;

import android.graphics.Bitmap;
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
import android.widget.ListView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.eventplanner.R;
import com.eventplanner.adapters.comments.CommentListAdapter;
import com.eventplanner.adapters.reviews.ReviewListAdapter;
import com.eventplanner.databinding.FragmentBusinessOwnerDetailsBinding;
import com.eventplanner.databinding.FragmentSolutionDetailsBinding;
import com.eventplanner.model.requests.reports.CreateReportRequest;
import com.eventplanner.model.responses.comments.GetCommentPreviewResponse;
import com.eventplanner.model.responses.reports.GetReportResponse;
import com.eventplanner.model.responses.reviews.GetReviewPreviewResponse;
import com.eventplanner.model.responses.users.GetUserProfilePictureResponse;
import com.eventplanner.model.responses.users.GetUserResponse;
import com.eventplanner.services.CommentService;
import com.eventplanner.services.ReportService;
import com.eventplanner.services.ReviewService;
import com.eventplanner.services.UserService;
import com.eventplanner.utils.Base64Util;
import com.eventplanner.utils.HttpUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;

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
    private ReviewService reviewService;
    private String businessOwnerId;
    private GetUserResponse businessOwner;
    private final String DEFAULT_IMAGE_URI= "http://10.0.2.2:8080/images/default-image.png"; // Static resource on backend

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
        reviewService = HttpUtils.getReviewService();
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

        // Image
        fetchAndSetUserProfilePicture();

        // on click opens dialog
        binding.reportUserButton.setOnClickListener(v -> {
            Log.i("BusinessOwnerFragment", "Attempt creating report.");
            showReportDialog();
        });
        // on click opens bottomSheetDialog for displaying comments|reviews
        binding.seeComments.setOnClickListener(v -> showCommentsDialog());
        binding.seeReviews.setOnClickListener(v -> showReviewsDialog());
    }

    private void fetchAndSetUserProfilePicture() {
        Call<GetUserProfilePictureResponse> call = userService.getUserProfilePictureBase64(Long.parseLong(businessOwnerId));

        call.enqueue(new Callback<GetUserProfilePictureResponse>() {
            @Override
            public void onResponse(Call<GetUserProfilePictureResponse> call, Response<GetUserProfilePictureResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String base64Image = response.body().getProfilePictureBase64();
                    if (base64Image != null && !base64Image.isEmpty()) {
                        Bitmap bitmap = Base64Util.decodeBase64ToBitmap(base64Image);
                        binding.imageUser.setImageBitmap(bitmap);
                    } else {
                        Glide.with(requireContext())
                                .load(DEFAULT_IMAGE_URI)
                                .into(binding.imageUser);
                    }
                } else {
                    Log.e("BusinessOwnerDetailsFragment", "Response failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GetUserProfilePictureResponse> call, Throwable t) {
                Log.e("BusinessOwnerDetailsFragment", "Network failure: " + t.getMessage(), t);
            }
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

    private void showCommentsDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_comments, null);
        ListView commentsListView = view.findViewById(R.id.commentsListView);

        fetchComments(Long.parseLong(businessOwnerId), commentsListView);

        dialog.setContentView(view);
        dialog.show();
    }

    private void showReviewsDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_reviews, null);
        ListView reviewsListView = view.findViewById(R.id.reviewsListView);

        fetchReviews(Long.parseLong(businessOwnerId), reviewsListView);

        dialog.setContentView(view);
        dialog.show();
    }

    private void fetchComments(Long businessOwnerId, ListView commentsListView) {
        Call<Collection<GetCommentPreviewResponse>> call = commentService.getAllCommentsByBusinessOwnerId(businessOwnerId);

        call.enqueue(new Callback<Collection<GetCommentPreviewResponse>>() {
            @Override
            public void onResponse(Call<Collection<GetCommentPreviewResponse>> call, Response<Collection<GetCommentPreviewResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Collection<GetCommentPreviewResponse> comments = response.body();
                    Log.i("BusinessOwnerDetailsFragment", "Number of comments fetched: " + comments.size());
                    CommentListAdapter adapter = new CommentListAdapter(getContext(), new ArrayList<>(comments));
                    commentsListView.setAdapter(adapter);

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

    private void fetchReviews(Long businessOwnerId, ListView reviewsListView) {
        Call<Collection<GetReviewPreviewResponse>> call = reviewService.getAllReviewsByBusinessOwnerId(businessOwnerId);

        call.enqueue(new Callback<Collection<GetReviewPreviewResponse>>() {
            @Override
            public void onResponse(Call<Collection<GetReviewPreviewResponse>> call, Response<Collection<GetReviewPreviewResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Collection<GetReviewPreviewResponse> reviews = response.body();
                    Log.i("BusinessOwnerDetailsFragment", "Number of reviews fetched: " + reviews.size());
                    ReviewListAdapter adapter = new ReviewListAdapter(getContext(), new ArrayList<>(reviews));
                    reviewsListView.setAdapter(adapter);
                } else {
                    Log.w("BusinessOwnerDetailsFragment", "Error while fetching reviews: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Collection<GetReviewPreviewResponse>> call, Throwable t) {
                Log.e("BusinessOwnerDetailsFragment", "Network failure: " + t.getMessage(), t);
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