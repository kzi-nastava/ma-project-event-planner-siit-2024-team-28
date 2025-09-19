package com.eventplanner.fragments.solutions;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.eventplanner.BuildConfig;
import com.eventplanner.R;
import com.eventplanner.databinding.FragmentSolutionDetailsBinding;
import com.eventplanner.model.constants.UserRoles;
import com.eventplanner.model.enums.ChatTheme;
import com.eventplanner.model.enums.DurationType;
import com.eventplanner.model.requests.chats.CreateChatRequest;
import com.eventplanner.model.requests.chats.FindChatRequest;
import com.eventplanner.model.requests.solutionComments.CreateSolutionCommentRequest;
import com.eventplanner.model.requests.solutionReviews.CreateSolutionReviewRequest;
import com.eventplanner.model.responses.ErrorResponse;
import com.eventplanner.model.responses.chats.FindChatResponse;
import com.eventplanner.model.responses.solutionComments.GetSolutionCommentResponse;
import com.eventplanner.model.responses.events.GetEventResponse;
import com.eventplanner.model.responses.solutionReviews.GetSolutionReviewResponse;
import com.eventplanner.model.responses.solutions.GetFavoriteSolutionResultResponse;
import com.eventplanner.model.responses.solutions.GetSolutionDetailsResponse;
import com.eventplanner.services.ChatService;
import com.eventplanner.services.SolutionCommentService;
import com.eventplanner.services.EventService;
import com.eventplanner.services.ProductService;
import com.eventplanner.services.SolutionReviewService;
import com.eventplanner.services.SolutionService;
import com.eventplanner.services.UserService;
import com.eventplanner.utils.AuthUtils;
import com.eventplanner.utils.Base64Util;
import com.eventplanner.utils.HttpUtils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SolutionDetailsFragment extends Fragment {
    private FragmentSolutionDetailsBinding binding;
    private String solutionId;
    private static final String ARG_SOLUTION_ID = "solutionId";
    private GetSolutionDetailsResponse solution;
    private SolutionService solutionService;
    private UserService userService;
    private EventService eventService;
    private ProductService productService;
    private SolutionCommentService solutionCommentService;
    private SolutionReviewService solutionReviewService;
    private ChatService chatService;
    private NavController navController;
    private int globalImageIndex = 0;


    public static SolutionDetailsFragment newInstance(String solutionId) {
        SolutionDetailsFragment fragment = new SolutionDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SOLUTION_ID, solutionId);
        fragment.setArguments(args);
        return fragment;
    }

    public SolutionDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        solutionService = HttpUtils.getSolutionService();
        userService = HttpUtils.getUserService();
        eventService = HttpUtils.getEventService();
        productService = HttpUtils.getProductService();
        solutionCommentService = HttpUtils.getCommentService();
        solutionReviewService = HttpUtils.getReviewService();
        chatService = HttpUtils.getChatService();
        navController = Navigation.findNavController(getActivity(), R.id.fragment_nav_content_main);
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            solutionId = getArguments().getString(ARG_SOLUTION_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate layout
        binding = FragmentSolutionDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // getting solution details from backend
        fetchSolutionDetails();
    }

    private void fetchSolutionDetails() {
        Long id = Long.parseLong(solutionId);
        Call<GetSolutionDetailsResponse> call = solutionService.getSolutionDetailsById(id);

        call.enqueue(new Callback<GetSolutionDetailsResponse>() {
            @Override
            public void onResponse(Call<GetSolutionDetailsResponse> call, Response<GetSolutionDetailsResponse> response) {
                if (response.isSuccessful()) {
                    solution = response.body();
                    if (solution != null) {
                        populateSolutionDetails();
                    }
                } else {
                    Log.e("SolutionDetailsFragment", "Error with fetching solution, response error code: " + response.code());
                    showErrorDialog();
                }
            }

            @Override
            public void onFailure(Call<GetSolutionDetailsResponse> call, Throwable t) {
                Log.e("SolutionDetailsFragment", "Network failure", t);
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

    private void addToFavorites(Long userId, Long solutionId) {
        Call<GetFavoriteSolutionResultResponse> call = userService.favoriteSolution(solutionId, userId);

        call.enqueue(new Callback<GetFavoriteSolutionResultResponse>() {
            @Override
            public void onResponse(Call<GetFavoriteSolutionResultResponse> call, Response<GetFavoriteSolutionResultResponse> response) {
                if (response.isSuccessful()) {
                    GetFavoriteSolutionResultResponse resultResponse = response.body();
                    Log.d("SolutionDetailsFragment", "Successfully added to favorites: " + resultResponse.getResultMessage());
                    // show message from backend
                    Toast.makeText(getContext(), resultResponse.getResultMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("SolutionDetailsFragment", "Error: " + response.code());
                    showErrorDialog();
                }
            }

            @Override
            public void onFailure(Call<GetFavoriteSolutionResultResponse> call, Throwable t) {
                Log.e("SolutionDetailsFragment", "Network failure", t);
                showErrorDialog();
            }
        });
    }

    /**
     *  Since buying logic depends on bunch of asynchronous responses:
     *                                                        - response from backend about active events
     *                                                        - response from user about selected event for which solution is buying
     *  'buy logic' is organized as a series of asynchronous event-driven callbacks (events -> response from backend & response from user)
     */
    private void buy() {
        // Even though "buy button" is hidden from every other user we check just in case
        if(!AuthUtils.getUserRoles(getContext()).contains("EventOrganizer"))
            return;

        // 'Solution' can be either 'Service' or 'Product' -> two different buying business logics
        if (solution.getType().equals("Product")) {
            // Fetching active events from backend and making a dialog for user
            fetchActiveEvents(
                    events -> showEventSelectionDialog(events, this::buyProduct) // buyProduct() as callback method
            );
        } else if (solution.getType().equals("Service")) {
            // Fetching active events from backend and making a dialog for user
            fetchActiveEvents(
                    events -> showEventSelectionDialog(events, this::buyService) // buyService() as callback method
            );
        }

    }

    /**
     * Fetches active events asynchronously from backend.
     * Calls onSuccess with the list of events on success.
     */
    private void fetchActiveEvents(Consumer<List<GetEventResponse>> onSuccess) {
        Long eventOrganizerId = AuthUtils.getUserId(getContext());
        List<Long> eventTypeIds = new ArrayList<>(solution.getEventTypeIds());

        Call<Collection<GetEventResponse>> call = eventService.getActiveEventsByTypeAndOrganizer(eventOrganizerId, eventTypeIds);
        call.enqueue(new Callback<Collection<GetEventResponse>>() {
            @Override
            public void onResponse(Call<Collection<GetEventResponse>> call, Response<Collection<GetEventResponse>> response) {
                if (response.isSuccessful()) {
                    List<GetEventResponse> events = new ArrayList<>(response.body());
                    Log.d("SolutionDetailsFragment", "Fetched events: " + events.size() + " events");
                    onSuccess.accept(events); // callback method call
                } else {
                    Log.e("SolutionDetailsFragment", "Failed fetching events: " + response.code());
                    Toast.makeText(getContext(), "Failed to load events", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Collection<GetEventResponse>> call, Throwable t) {
                Log.e("SolutionDetailsFragment", "Network error", t);
                Toast.makeText(getContext(), "Network failure", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Shows dialog for user to select an event.
     * Calls onResponse with the selected event ID.
     */
    private void showEventSelectionDialog(List<GetEventResponse> events, Consumer<Long> onResponse) {
        if (events == null || events.isEmpty()) {
            new AlertDialog.Builder(getContext())
                    .setTitle("No Events Available")
                    .setMessage("Currently, there are no active events to choose from.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        String[] eventNames = new String[events.size()];
        for (int i = 0; i < events.size(); i++) {
            eventNames[i] = events.get(i).getName();
        }

        final int[] selectedIndex = {0}; // first one is selected by default just because

        new AlertDialog.Builder(getContext())
                .setTitle("Select Event")
                .setSingleChoiceItems(eventNames, 0, (dialog, which) -> selectedIndex[0] = which)
                .setPositiveButton("OK", (dialog, which) -> {
                    Long selectedEventId = events.get(selectedIndex[0]).getId();
                    onResponse.accept(selectedEventId);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Function for making call to backend for buying a product
     */
    private void buyProduct(Long selectedEventId) {
        Long productId = solution.getId();

        productService.buyProduct(productId, selectedEventId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Product purchased successfully!", Toast.LENGTH_SHORT).show();
                    Log.i("SolutionDetailsFragment", "Buying product went successfully.");
                } else {
                    try {
                        String errorJson = response.errorBody().string();
                        Gson gson = new Gson();
                        ErrorResponse errorResponse = gson.fromJson(errorJson, ErrorResponse.class);
                        Toast.makeText(getContext(), errorResponse.getError(), Toast.LENGTH_SHORT).show();
                        Log.i("SolutionDetailsFragment", "Purchase failed: " + errorResponse.getError());
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Purchase failed: unknown error", Toast.LENGTH_SHORT).show();
                        Log.i("SolutionDetailsFragment", "Purchase failed: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Network error while purchasing product", Toast.LENGTH_SHORT).show();
                Log.i("SolutionDetailsFragment", "Network failure: " + t.getMessage());
            }
        });
    }

    /**
     * Function for making call to backend for buying a service
     */
    private void buyService(Long selectedEventId) {
        // TODO: make call to backend
    }

    private void commentAndReview() {
        if(!AuthUtils.getUserRoles(getContext()).contains(UserRoles.EventOrganizer))
            return;

        canUserCommentReview(canCommentReview -> showCommentReviewDialog(canCommentReview, this::createComment, this::createReview));
    }

    private void canUserCommentReview(Consumer<Boolean> callback) {
        Long eventOrganizerId = AuthUtils.getUserId(getContext());
        solutionService.canUserCommentReview(Long.parseLong(solutionId), eventOrganizerId)
                .enqueue(new Callback<Boolean>() {
                    @Override
                    public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                        if (response.isSuccessful()) {
                            Boolean canCommentOrReview = response.body();
                            callback.accept(canCommentOrReview);
                        } else {
                            Log.e("SolutionDetailsFragment", "Failed: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<Boolean> call, Throwable t) {
                        Log.e("SolutionDetailsFragment", "Network error", t);
                    }
                });
    }

    private void showCommentReviewDialog(boolean canReviewComment, Consumer<CreateSolutionCommentRequest> createComment, Consumer<CreateSolutionReviewRequest> createReview) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        if (canReviewComment) {
            // Inflate custom layout
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_comment_review, null);
            EditText commentField = dialogView.findViewById(R.id.editText_comment);
            RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);

            builder.setTitle("Leave a review")
                    .setView(dialogView)
                    .setPositiveButton("Submit", (dialog, which) -> {
                        String commentText = commentField.getText().toString().trim();
                        float rating = ratingBar.getRating();

                        if (commentText.isEmpty()) {
                            Toast.makeText(getContext(), "Comment cannot be empty.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (rating < 1.0f) {
                            Toast.makeText(getContext(), "Please provide a rating between 1 and 5.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Long userId = AuthUtils.getUserId(getContext());
                        Long solId = Long.parseLong(solutionId);

                        CreateSolutionCommentRequest commentRequest = CreateSolutionCommentRequest.builder()
                                .commenterId(userId)
                                .solutionId(solId)
                                .content(commentText)
                                .build();
                        createComment.accept(commentRequest);

                        CreateSolutionReviewRequest reviewRequest = CreateSolutionReviewRequest.builder()
                                .reviewerId(userId)
                                .solutionId(solId)
                                .rating((short) rating)
                                .build();
                        createReview.accept(reviewRequest);
                    })
                    .setNegativeButton("Cancel", null);
        } else {
            builder.setTitle("Not Allowed")
                    .setMessage("You can only leave a comment and review after purchasing this solution.")
                    .setPositiveButton("OK", null);
        }

        builder.show();
    }

    private void createComment(CreateSolutionCommentRequest request) {
        solutionCommentService.createComment(request).enqueue(new Callback<GetSolutionCommentResponse>() {
            @Override
            public void onResponse(Call<GetSolutionCommentResponse> call, Response<GetSolutionCommentResponse> response) {
                if (response.isSuccessful()) {
                    GetSolutionCommentResponse comment = response.body();
                    Toast.makeText(getContext(), "Comment created successfully!", Toast.LENGTH_SHORT).show();
                    Log.i("SolutionDetailsFragment", "Comment created ID: " + comment.getId());
                } else {
                    try {
                        String errorJson = response.errorBody().string();
                        Gson gson = new Gson();
                        ErrorResponse errorResponse = gson.fromJson(errorJson, ErrorResponse.class);
                        Toast.makeText(getContext(), "Error creating comment: " + errorResponse.getError(), Toast.LENGTH_SHORT).show();
                        Log.e("SolutionDetailsFragment", "Server error: " + response.code() + " - " + errorResponse.getError());
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Unknown error", Toast.LENGTH_SHORT).show();
                        Log.e("SolutionDetailsFragment", "Error : " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(Call<GetSolutionCommentResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Network failure: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("CommentActivity", "Network failure: " + t.getMessage(), t);
            }
        });
    }
    private void createReview(CreateSolutionReviewRequest request) {
            solutionReviewService.createReview(request).enqueue(new Callback<GetSolutionReviewResponse>() {
                @Override
                public void onResponse(Call<GetSolutionReviewResponse> call, Response<GetSolutionReviewResponse> response) {
                    if (response.isSuccessful()) {
                        GetSolutionReviewResponse review = response.body();
                        Toast.makeText(getContext(), "Review created successfully!", Toast.LENGTH_SHORT).show();
                        Log.i("ReviewFragment", "Review created ID: " + review.getId());
                    } else {
                        try {
                            String errorJson = response.errorBody().string();
                            Gson gson = new Gson();
                            ErrorResponse errorResponse = gson.fromJson(errorJson, ErrorResponse.class);
                            Toast.makeText(getContext(), "Error creating review: " + errorResponse.getError(), Toast.LENGTH_SHORT).show();
                            Log.e("ReviewFragment", "Server error: " + response.code() + " - " + errorResponse.getError());
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Unknown error", Toast.LENGTH_SHORT).show();
                            Log.e("ReviewFragment", "Error : " + response.code());
                        }
                    }
                }

                @Override
                public void onFailure(Call<GetSolutionReviewResponse> call, Throwable t) {
                    Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("ReviewFragment", "Network failure: " + t.getMessage(), t);
                }
            });
    }

    /**
     * Method that contains chatWithOwner logic
     * On click either routes to existing chat or creates new chat with business owner
     * */
    private void chatWithOwner() {
        if(!AuthUtils.getUserRoles(getContext()).contains("EventOrganizer"))
            return;

        findChat();
    }

    private void navigateToChat(Long chatId) {
        Bundle bundle = new Bundle();
        bundle.putLong("chatId", chatId);
        navController.navigate(R.id.action_solutionDetails_to_chat, bundle);
    }

    /**
     * Function for making call to backend to find chat by participants and theme (SOLUTION always)
     * If chat is found navigate to chat
     * If chat is not found create and navigate to a new chat
     * */
    private void findChat() {
        FindChatRequest request = new FindChatRequest(AuthUtils.getUserId(getContext()), solution.getBusinessOwnerId(), ChatTheme.SOLUTION, Long.parseLong(solutionId));
        Call<FindChatResponse> call = chatService.getChatByParticipantsAndTheme(request);

        call.enqueue(new Callback<FindChatResponse>() {
            @Override
            public void onResponse(Call<FindChatResponse> call, Response<FindChatResponse> response) {
                if (response.isSuccessful()) {
                    FindChatResponse result = response.body();
                    if(result != null && result.isFound()) {
                        navigateToChat(result.getChat().getId());
                    }
                    else {
                        createChat();
                    }
                } else {
                    try {
                        String errorJson = response.errorBody().string();
                        ErrorResponse errorResponse = new Gson().fromJson(errorJson, ErrorResponse.class);
                        Toast.makeText(getContext(), errorResponse.getError(), Toast.LENGTH_SHORT).show();
                        Log.e("SolutionDetailsFragment", "Find chat failed: " + errorResponse.getError());
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Find chat failed: " + response.code(),
                                Toast.LENGTH_SHORT).show();
                        Log.e("SolutionDetailsFragment", "Find chat failed", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<FindChatResponse> call, Throwable t) {
                Log.e("SolutionDetailsFragmentt", "Network failure: " + t.getMessage());
                Toast.makeText(getContext(), "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Function for making request to backend for creating new chat
     * After successfully creating, navigates to newly created chat
     * */
    private void createChat() {
        // Creating request
        CreateChatRequest request = new CreateChatRequest.Builder()
                .participant1Id(AuthUtils.getUserId(getContext()))
                .participant2Id(solution.getBusinessOwnerId())
                .theme(ChatTheme.SOLUTION)
                .themeId(Long.parseLong(solutionId))
                .build();

        Call<Long> call = chatService.createChat(request);

        call.enqueue(new Callback<Long>() {
            @Override
            public void onResponse(Call<Long> call, Response<Long> response) {
                if (response.isSuccessful()) {
                    Long chatId = response.body();
                    Toast.makeText(getContext(), "Chat created with ID: " + chatId, Toast.LENGTH_SHORT).show();
                    Log.e("SolutionDetailsFragment", "Chat created with ID: " + chatId);
                    navigateToChat(chatId);
                } else {
                    try {
                        String errorJson = response.errorBody().string();
                        ErrorResponse errorResponse = new Gson().fromJson(errorJson, ErrorResponse.class);
                        Toast.makeText(getContext(), errorResponse.getError(), Toast.LENGTH_SHORT).show();
                        Log.e("SolutionDetailsFragment", "Create failed: " + errorResponse.getError());
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Create failed: Unknown error", Toast.LENGTH_SHORT).show();
                        Log.e("SolutionDetailsFragment", "Create failed: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(Call<Long> call, Throwable t) {
                Log.e("ChatFragment", "Network failure: " + t.getMessage());
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // method used to separate code for altering views from main code
    // a bit messy
    private void populateSolutionDetails() {
        populateBasicInfo();
        populatePriceInfo();
        if (solution.getType().equals("Service")) {
            populateServiceSpecifics();
        } else {
            hideServiceSpecificViews();
        }

        // setting routing to business owner page
        binding.visitOwnerPageButton.setOnClickListener(v -> {
            Log.i("SolutionDetailsFragment", "Visiting business owner page for id: " + solution.getBusinessOwnerId());
            Bundle bundle = new Bundle();
            bundle.putString("businessOwnerId", String.valueOf(solution.getBusinessOwnerId()));
            navController.navigate(R.id.action_solutionDetails_to_businessOwnerDetails, bundle);
        });

        // Button for buying should be only visible to EventOrganizers
        List<String> roles = AuthUtils.getUserRoles(getContext());
        if (!solution.getIsAvailable() || !roles.contains(UserRoles.EventOrganizer))
            binding.buttonBuy.setVisibility(View.GONE);

        binding.buttonBuy.setOnClickListener( v -> {
            buy();
        });

        // button for adding to favorites should be only visible to EventOrganizers
        if(roles.contains(UserRoles.EventOrganizer)) {
            binding.addToFavorites.setOnClickListener(v -> {
                Long userId = AuthUtils.getUserId(getContext());
                addToFavorites(userId, Long.parseLong(solutionId));
            });
        } else
            binding.addToFavorites.setVisibility(View.GONE);


        // Button for commenting/reviewing should be only visible to EventOrganizers
        if(roles.contains(UserRoles.EventOrganizer)) {
            binding.buttonReview.setOnClickListener(v -> {
                commentAndReview();
            });
        } else
            binding.buttonReview.setVisibility(View.GONE);

        if(roles.contains(UserRoles.EventOrganizer)) {
            binding.buttonChatWithOwner.setOnClickListener(v -> {
                chatWithOwner();
            });
        } else
            binding.buttonChatWithOwner.setVisibility(View.GONE);
    }

    private void populateBasicInfo() {
        binding.textTitle.setText(solution.getName());
        binding.textCategory.setText(solution.getCategoryName());
        binding.textAuthor.setText(solution.getBusinessOwnerName());

        // Image
        if (solution.getImagesBase64() != null) {
            if (solution.getImagesBase64().isEmpty()) {
                // Glide is a library for efficient loading and displaying of images from various sources (URL, Base64, files, etc.),
                // which automatically caches images and optimizes memory usage. It helps load images quickly and smoothly without blocking the UI.
                Glide.with(requireContext())
                        .load(Base64Util.DEFAULT_IMAGE_URI)
                        .into(binding.imageSolution);
            } else {
                // Set first picture as current one
                Bitmap bitmap = Base64Util.decodeBase64ToBitmap(solution.getImagesBase64().get(globalImageIndex));
                binding.imageSolution.setImageBitmap(bitmap);
                // Listeners for buttons that cycle through pictures
                binding.buttonPreviousImage.setOnClickListener(this::changeImage);
                binding.buttonNextImage.setOnClickListener(this::changeImage);
            }
            // If there is no more than one picture hide buttons for cycling
            if (!(solution.getImagesBase64().size() > 1)) {
                binding.buttonPreviousImage.setVisibility(View.GONE);
                binding.buttonNextImage.setVisibility(View.GONE);
            }
        }

        String availabilityStatus = (solution.getIsAvailable()) ? "Available" : "Unavailable";
        binding.textAvailability.setText(binding.textAvailability.getText() + ": " + availabilityStatus);
        binding.textDescription.setText(binding.textDescription.getText() + ": " + solution.getDescription());
        binding.textEventTypes.setText(binding.textEventTypes.getText() + ": " + solution.getEventTypeNames().stream()
                .map(String::valueOf).collect(Collectors.joining(", ")));
    }

    private void populatePriceInfo() {
        binding.textPrice.setText(String.format("Price: %.2f$", solution.getPrice()));
        if (solution.getDiscount() > 0) {
            binding.textDiscount.setText(String.format("Discounted Price: %.2f$", calculateFinalPrice(solution.getPrice(), solution.getDiscount())));
            binding.textPrice.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray));
            binding.textDiscount.setVisibility(View.VISIBLE);
        } else {
            binding.textDiscount.setVisibility(View.GONE);
        }
    }

    private void populateServiceSpecifics() {
        binding.textSpecifics.setText("Specifics: " + solution.getSpecifics());
        if (solution.getDurationType() == DurationType.FIXED) {
            binding.textMinDuration.setVisibility(View.GONE);
            binding.textMaxDuration.setVisibility(View.GONE);
            binding.textDuration.setText(String.format("Duration: %.2f hrs", convertSecondsToHours(solution.getFixedDurationInSeconds())));
        } else {
            binding.textDuration.setVisibility(View.GONE);
            binding.textMinDuration.setText(String.format("Min Duration: %.2f hrs", convertSecondsToHours(solution.getMinDurationInSeconds())));
            binding.textMaxDuration.setText(String.format("Max Duration: %.2f hrs", convertSecondsToHours(solution.getMaxDurationInSeconds())));
        }
        binding.textReservationDeadline.setText("Reservation Deadline: " + solution.getReservationDeadlineDays() + " days beforehand");
        binding.textCancellationDeadline.setText("Cancellation Deadline: " + solution.getCancellationDeadlineDays() + " days beforehand");
        binding.textReservationType.setText("Reservation Type: " + solution.getReservationType());
    }

    private void hideServiceSpecificViews() {
        binding.textSpecifics.setVisibility(View.GONE);
        binding.textDuration.setVisibility(View.GONE);
        binding.textMinDuration.setVisibility(View.GONE);
        binding.textMaxDuration.setVisibility(View.GONE);
        binding.textReservationDeadline.setVisibility(View.GONE);
        binding.textCancellationDeadline.setVisibility(View.GONE);
        binding.textReservationType.setVisibility(View.GONE);
    }

    // Changes image in image view
    // Operates with globalImageIndex -> ++ if buttonNextImage; -- if buttonPreviousImage
    private void changeImage(View v) {
        int lastIndex = solution.getImagesBase64().size() - 1;
        // If buttonPreviousImage is clicked
        if (v == binding.buttonPreviousImage) {
            // If current image is the first one and previous is clicked -> set last image as current one
            if (globalImageIndex == 0)
                globalImageIndex = lastIndex;
            else
                globalImageIndex--;
        }
        // If buttonNextImage is clicked
        if (v == binding.buttonNextImage) {
            // If current image is the last one and next is clicked -> set first image as current one
            if (globalImageIndex == lastIndex)
                globalImageIndex = 0;
            else
                globalImageIndex++;
        }
        Bitmap bitmap = Base64Util.decodeBase64ToBitmap(solution.getImagesBase64().get(globalImageIndex));
        binding.imageSolution.setImageBitmap(bitmap);
    }

    private Double convertSecondsToHours(Integer seconds) {
        Integer hours = seconds / 3600;
        Integer minutes = (seconds % 3600) / 60;

        Double decimalHours = hours + (minutes / 60.0);
        return decimalHours;
    }

    private Double calculateFinalPrice(Double originalPrice, Double discountPercent) {
        Double discountAmount = originalPrice * (discountPercent / 100.0);
        return originalPrice - discountAmount;
    }
}