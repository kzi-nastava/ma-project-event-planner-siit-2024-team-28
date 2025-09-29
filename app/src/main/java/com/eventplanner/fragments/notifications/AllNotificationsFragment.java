package com.eventplanner.fragments.notifications;

import static com.eventplanner.utils.NotificationActionParser.parse;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eventplanner.R;
import com.eventplanner.adapters.notifications.NotificationsAdapter;
import com.eventplanner.model.responses.PagedResponse;
import com.eventplanner.model.responses.notifications.GetNotificationResponse;
import com.eventplanner.model.responses.notifications.GetNotificationCountResponse;
import com.eventplanner.services.NotificationService;
import com.eventplanner.services.NotificationWebSocketService;
import com.eventplanner.utils.AuthUtils;
import com.eventplanner.utils.HttpUtils;
import com.eventplanner.utils.NotificationActionParser;
import com.eventplanner.utils.NotificationManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

import java.time.LocalDateTime;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllNotificationsFragment extends Fragment {

    private static final int PAGE_SIZE = 10;

    private LinearLayout emptyState;
    private ProgressBar loadingState;
    private RecyclerView recycler;
    private Button prevPageBtn;
    private Button nextPageBtn;

    private NotificationsAdapter adapter;
    private NotificationService notificationService;
    private long currentUserId;

    private int currentPage = 0;
    private int totalPages = 0;

    private NavController navController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_all_notifications, container, false);

        emptyState = root.findViewById(R.id.empty_state);
        loadingState = root.findViewById(R.id.loading_state);
        recycler = root.findViewById(R.id.recycler_notifications);
        prevPageBtn = root.findViewById(R.id.prev_page_btn);
        nextPageBtn = root.findViewById(R.id.next_page_btn);

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationsAdapter();
        recycler.setAdapter(adapter);

        // handle click
        adapter.setOnItemClickListener(this::onNotificationClick);

        // services
        notificationService = HttpUtils.getNotificationService();
        currentUserId = AuthUtils.getUserId(requireContext());

        prevPageBtn.setOnClickListener(v -> {
            if (currentPage > 0) {
                currentPage--;
                loadNotifications(currentPage);
            }
        });

        nextPageBtn.setOnClickListener(v -> {
            if (currentPage + 1 < totalPages) {
                currentPage++;
                loadNotifications(currentPage);
            }
        });

        if (currentUserId > 0) {
            loadNotifications(0);
        } else {
            showEmptyState();
        }
        return root;
    }

    private void loadNotifications(int page) {
        showLoading();

        notificationService.getUserNotifications(currentUserId, page, PAGE_SIZE)
                .enqueue(new Callback<PagedResponse<GetNotificationResponse>>() {
                    @Override
                    public void onResponse(Call<PagedResponse<GetNotificationResponse>> call,
                                           Response<PagedResponse<GetNotificationResponse>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<GetNotificationResponse> items = response.body().getContent();
                            totalPages = response.body().getTotalPages();

                            if (items == null || items.isEmpty()) {
                                showEmptyState();
                            } else {
                                adapter.setNotifications(items);
                                showList();
                            }
                            updatePaginationButtons();
                        } else {
                            showEmptyState();
                        }
                    }

                    @Override
                    public void onFailure(Call<PagedResponse<GetNotificationResponse>> call, Throwable t) {
                        Toast.makeText(getContext(), "Failed to load notifications", Toast.LENGTH_SHORT).show();
                        showEmptyState();
                    }
                });
    }

    private void updatePaginationButtons() {
        prevPageBtn.setEnabled(currentPage > 0);
        nextPageBtn.setEnabled(currentPage + 1 < totalPages);
    }

    private void showLoading() {
        loadingState.setVisibility(View.VISIBLE);
        recycler.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        loadingState.setVisibility(View.GONE);
        recycler.setVisibility(View.GONE);
        emptyState.setVisibility(View.VISIBLE);
    }

    private void showList() {
        loadingState.setVisibility(View.GONE);
        recycler.setVisibility(View.VISIBLE);
        emptyState.setVisibility(View.GONE);
    }

    private void onNotificationClick(GetNotificationResponse notification) {
        if (!TextUtils.isEmpty(notification.getActionUrl())) {
            handleAction(notification.getActionUrl());
        }
    }

    public void handleAction(String actionUrl) {
        NotificationActionParser.ParsedAction action = parse(actionUrl);
        NavController navController = Navigation.findNavController(requireView());

        Bundle bundle = new Bundle();
        switch (action.route) {
            case "event":
                bundle.putLong("eventId", Long.parseLong(action.id));
                navController.navigate(R.id.nav_event, bundle);
                break;

            case "solution":
                bundle.putString("solutionId", action.id);
                navController.navigate(R.id.nav_solution_details, bundle);
                break;

            case "businessOwner":
                bundle.putString("businessOwnerId", action.id);
                navController.navigate(R.id.business_owner_details_fragment, bundle);
                break;

            case "categories":
                navController.navigate(R.id.nav_categories_overview, bundle);
                break;

            default:
                // fallback, do nothing or log
                break;
        }
    }
}
