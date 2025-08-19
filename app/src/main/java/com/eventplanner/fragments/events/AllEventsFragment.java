package com.eventplanner.fragments.events;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.appcompat.widget.SearchView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.eventplanner.R;
import com.eventplanner.adapters.events.EventListAdapter;
import com.eventplanner.model.constants.UserRoles;
import com.eventplanner.model.responses.PagedResponse;
import com.eventplanner.model.responses.events.GetEventResponse;
import com.eventplanner.services.EventService;
import com.eventplanner.utils.AuthUtils;
import com.eventplanner.utils.HttpUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllEventsFragment extends Fragment {
    private EventService eventService;
    private List<GetEventResponse> events = new ArrayList<>();
    private final List<String> eventTypes = new ArrayList<>();
    private String searchTerm = "";
    private String selectedEventType = "All";
    private String activeSort = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        HttpUtils.initialize(getContext());

        View rootView = inflater.inflate(R.layout.fragment_all_events, container, false);
        eventService = HttpUtils.getEventService();

        // Setup UI components
        setupSearchView(rootView);
        setupEventTypeSpinner(rootView);
        setupSortButtons(rootView);
        setupPaginationButtons(rootView);
        setupCreateEventButton(rootView);

        // Load initial data
        loadEvents();

        return rootView;
    }

    private void setupSearchView(View rootView) {
        SearchView searchView = rootView.findViewById(R.id.search_bar);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchTerm = query;
                loadEvents();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchTerm = newText;
                loadEvents();
                return false;
            }
        });
    }

    private void setupEventTypeSpinner(View rootView) {
        Spinner eventTypeSpinner = rootView.findViewById(R.id.event_type_spinner);
        eventTypes.add("All");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                eventTypes
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eventTypeSpinner.setAdapter(adapter);

        eventTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedEventType = eventTypes.get(position);
                loadEvents();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupSortButtons(View rootView) {
        Button closestButton = rootView.findViewById(R.id.button_sort_closest);
        Button furthestButton = rootView.findViewById(R.id.button_sort_furthest);

        closestButton.setOnClickListener(v -> {
            activeSort = activeSort.equals("closest") ? "" : "closest";
            updateButtonStyles(closestButton, furthestButton);
            loadEvents();
        });

        furthestButton.setOnClickListener(v -> {
            activeSort = activeSort.equals("furthest") ? "" : "furthest";
            updateButtonStyles(closestButton, furthestButton);
            loadEvents();
        });
    }

    private void updateButtonStyles(Button closestButton, Button furthestButton) {
        closestButton.setSelected(activeSort.equals("closest"));
        furthestButton.setSelected(activeSort.equals("furthest"));
    }

    private void setupPaginationButtons(View rootView) {
        // Setup pagination buttons similar to Angular component
        // Implement previous/next page logic here
    }

    private void setupCreateEventButton(View rootView) {
        if (AuthUtils.getUserRoles(requireContext()).contains(UserRoles.EventOrganizer)) {
            Button createButton = rootView.findViewById(R.id.button_create_event);
            createButton.setVisibility(View.VISIBLE);
            createButton.setOnClickListener(v -> {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.fragment_nav_content_main);
                navController.navigate(R.id.nav_event);
            });
        }
    }

    private void loadEvents() {
        int currentPage = 0;
        int pageSize = 10;
        eventService.getAllPublicEvents(currentPage, pageSize).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<PagedResponse<GetEventResponse>> call, @NonNull Response<PagedResponse<GetEventResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PagedResponse<GetEventResponse> pagedResponse = response.body();
                    events = pagedResponse.getContent();
                    updateEventTypes();
                    updateEventList();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PagedResponse<GetEventResponse>> call, @NonNull Throwable t) {
                // Handle error
            }
        });
    }

    private void updateEventTypes() {
        Set<String> uniqueTypes = new HashSet<>();
        uniqueTypes.add("All");
        for (GetEventResponse event : events) {
            if (event.getEventTypeName() != null) {
                uniqueTypes.add(event.getEventTypeName());
            }
        }
        eventTypes.clear();
        eventTypes.addAll(uniqueTypes);
    }

    private void updateEventList() {
        ListView listView = getView().findViewById(android.R.id.list);
        EventListAdapter adapter = new EventListAdapter(requireContext(), filterAndSortEvents());
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            GetEventResponse event = (GetEventResponse) parent.getItemAtPosition(position);
            NavController navController = Navigation.findNavController(requireActivity(), R.id.fragment_nav_content_main);
            Bundle args = new Bundle();
            args.putLong("eventId", event.getId());
            navController.navigate(R.id.action_all_events_to_event, args);
        });
    }

    private List<GetEventResponse> filterAndSortEvents() {
        List<GetEventResponse> filtered = new ArrayList<>(events);

        // Filter by event type
        if (!selectedEventType.equals("All")) {
            filtered.removeIf(event ->
                    !selectedEventType.equals(event.getEventTypeName())
            );
        }

        // Filter by search term
        if (!searchTerm.isEmpty()) {
            filtered.removeIf(event ->
                    !event.getName().toLowerCase().contains(searchTerm.toLowerCase())
            );
        }

        // Sort events
        if (activeSort.equals("closest")) {
            filtered.sort(Comparator.comparing(GetEventResponse::getStartDate));
        } else if (activeSort.equals("furthest")) {
            filtered.sort((a, b) -> b.getStartDate().compareTo(a.getStartDate()));
        }

        return filtered;
    }
}