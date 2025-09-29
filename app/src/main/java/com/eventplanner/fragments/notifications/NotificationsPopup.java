package com.eventplanner.fragments.notifications;

import static com.eventplanner.utils.NotificationActionParser.parse;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eventplanner.R;
import com.eventplanner.adapters.notifications.NotificationsAdapter;
import com.eventplanner.model.responses.notifications.GetNotificationResponse;
import com.eventplanner.services.NotificationService;
import com.eventplanner.utils.AuthUtils;
import com.eventplanner.utils.HttpUtils;
import com.eventplanner.utils.NotificationActionParser;
import com.eventplanner.utils.NotificationManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsPopup {
    private PopupWindow popupWindow;
    private Context context;
    private NotificationsAdapter adapter;
    private NotificationService notificationService;
    private NotificationManager notificationManager;
    private TextView emptyView;

    private NavController navController;

    public NotificationsPopup(Context context, NavController navController) {
        this.notificationManager = NotificationManager.getInstance(context);
        this.context = context;
        this.notificationService = HttpUtils.getNotificationService();
        this.navController = navController;
        setupPopup();
    }

    private void setupPopup() {
        View popupView = LayoutInflater.from(context).inflate(R.layout.popup_notifications, null);

        popupWindow = new PopupWindow(
                popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
        );

        // Set background to make popup dismissible when touching outside
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        popupWindow.setElevation(16f);

        RecyclerView recycler = popupView.findViewById(R.id.recycler_notifications);
        recycler.setLayoutManager(new LinearLayoutManager(context));
        adapter = new NotificationsAdapter();
        adapter.setOnItemClickListener(notification -> {
            onNotificationClick(notification);
        });
        recycler.setAdapter(adapter);

        emptyView = popupView.findViewById(R.id.empty_view);

        Button markAllReadBtn = popupView.findViewById(R.id.mark_all_read_btn);
        Button viewAllBtn = popupView.findViewById(R.id.view_all_btn);

        markAllReadBtn.setOnClickListener(v -> markAllAsRead());
        viewAllBtn.setOnClickListener(v -> {
            // Navigate to full notifications fragment
            NavController navController = Navigation.findNavController((Activity) context, R.id.fragment_nav_content_main);
            navController.navigate(R.id.nav_all_notifications_fragment);
            dismiss();
        });

        notificationManager.getNotificationsLiveData().observeForever(notificationsObserver);
    }
    private final Observer<List<GetNotificationResponse>> notificationsObserver = new Observer<List<GetNotificationResponse>>() {
        @Override
        public void onChanged(List<GetNotificationResponse> notifications) {
            if (notifications == null || notifications.isEmpty()) {
                adapter.setNotifications(new ArrayList<>());
                emptyView.setVisibility(View.VISIBLE);
            } else {
                adapter.setNotifications(notifications);
                emptyView.setVisibility(View.GONE);
            }
        }
    };

    public void show(View anchorView) {
        popupWindow.showAsDropDown(anchorView, -250, 0);
    }

    public void dismiss() {
        popupWindow.dismiss();
    }

    private void markAllAsRead() {
        long userId = AuthUtils.getUserId(context);
        notificationService.markAllAsRead(userId).enqueue(
                new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        notificationManager.popAllNotifications();
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {

                    }
                    // Implement callback
                }
        );
    }
    private void onNotificationClick(GetNotificationResponse notification) {
        if (!notification.getIsRead()) {
            markAsRead(notification);
        }

        if (!TextUtils.isEmpty(notification.getActionUrl())) {
            handleAction(notification.getActionUrl());
        }

        dismiss();
    }
    public void handleAction(String actionUrl) {
        NotificationActionParser.ParsedAction action = parse(actionUrl);
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
    private void markAsRead(GetNotificationResponse notification) {
        notificationService.markAsRead(notification.getId(), AuthUtils.getUserId(context)).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                notification.setIsRead(true);
                notificationManager.popNotification(notification);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(context, "Failed to mark all as read", Toast.LENGTH_SHORT).show();

            }
        });
    }
}