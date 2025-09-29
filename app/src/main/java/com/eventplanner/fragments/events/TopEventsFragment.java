package com.eventplanner.fragments.events;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.eventplanner.R;
import com.eventplanner.adapters.events.TopEventListAdapter;
import com.eventplanner.model.responses.PagedResponse;
import com.eventplanner.model.responses.events.GetEventResponse;
import com.eventplanner.services.EventService;
import com.eventplanner.utils.HttpUtils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TopEventsFragment extends Fragment {
    private EventService eventService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        HttpUtils.initialize(getContext());

        View rootView = inflater.inflate(R.layout.fragment_top_events, container, false);
        eventService = HttpUtils.getEventService();

        loadTopEvents(rootView);

        Button browseEventsButton = rootView.findViewById(R.id.browse_events_button);
        browseEventsButton.setOnClickListener(v -> navigateToAllEvents());

        return rootView;
    }

    private void loadTopEvents(View rootView) {
        eventService.getTopEvents().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<PagedResponse<GetEventResponse>> call,
                                   @NonNull Response<PagedResponse<GetEventResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<GetEventResponse> events = response.body().getContent();
                    setupEventList(rootView, events);
                }
            }

            @Override
            public void onFailure(@NonNull Call<PagedResponse<GetEventResponse>> call,
                                  @NonNull Throwable t) {
                // Handle error
            }
        });
    }

    private void setupEventList(View rootView, List<GetEventResponse> events) {
        ListView listView = rootView.findViewById(android.R.id.list);
        TopEventListAdapter adapter = new TopEventListAdapter(requireContext(), events);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            GetEventResponse event = (GetEventResponse) parent.getItemAtPosition(position);
            navigateToEventDetails(event.getId());
        });
    }

    private void navigateToEventDetails(long eventId) {
        NavController navController = Navigation.findNavController(requireActivity(),
                R.id.fragment_nav_content_main);
        Bundle args = new Bundle();
        args.putLong("eventId", eventId);
        navController.navigate(R.id.action_home_to_event, args);
    }

    private void navigateToAllEvents() {
        NavController navController = Navigation.findNavController(requireActivity(),
                R.id.fragment_nav_content_main);
        navController.navigate(R.id.action_home_to_all_events);
    }
}