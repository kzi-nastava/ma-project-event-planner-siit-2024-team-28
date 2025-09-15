package com.eventplanner.fragments.events;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import com.eventplanner.R;
import com.eventplanner.adapters.events.AllEventsAdapter;
import com.eventplanner.model.constants.UserRoles;
import com.eventplanner.model.responses.PagedResponse;
import com.eventplanner.model.responses.eventTypes.GetEventTypeResponse;
import com.eventplanner.model.responses.events.GetEventResponse;
import com.eventplanner.services.EventService;
import com.eventplanner.services.EventTypeService;
import com.eventplanner.utils.AuthUtils;
import com.eventplanner.utils.HttpUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllEventsFragment extends Fragment implements AllEventsAdapter.OnEventClickListener {

    private EventService eventService;
    private EventTypeService eventTypeService;
    private AllEventsAdapter adapter;
    private List<GetEventResponse> events = new ArrayList<>();

    // Pagination
    private int totalPages = 0;
    private int currentPage = 0;
    private int pageSize = 10;

    private TextView textCurrentPage;
    private EditText editPageNumber;
    private Spinner spinnerPageSize;

    // Filters
    private String searchQuery;
    private Long selectedEventTypeId;
    private Integer minParticipants;
    private Integer maxParticipants;
    private LocalDate startDate;
    private LocalDate endDate;
    private String sort;

    // Event types cache (for dialog)
    private List<GetEventTypeResponse> eventTypes = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_all_events, container, false);

        eventService = HttpUtils.getEventService();
        eventTypeService = HttpUtils.getEventTypeService();

        setupPagination(rootView);
        setupRecyclerView(rootView);
        setupSearchView(rootView);
        setupCreateEventFab(rootView);
        setupEventTypes();

        // Filter + clear buttons
        Button filterButton = rootView.findViewById(R.id.button_filter_products);
        if (filterButton != null) {
            filterButton.setOnClickListener(v -> showFilterDialog());
        }

        Button clearFiltersButton = rootView.findViewById(R.id.button_clear_filters);
        if (clearFiltersButton != null) {
            clearFiltersButton.setOnClickListener(v -> clearFilters());
        }

        loadEvents();
        return rootView;
    }

    private void setupEventTypes()
    {
        eventTypeService.getAllEventTypes().enqueue(new Callback<Collection<GetEventTypeResponse>>() {
            @Override
            public void onResponse(Call<Collection<GetEventTypeResponse>> call, Response<Collection<GetEventTypeResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    eventTypes = new ArrayList<>(response.body());
                    Log.d("AllProductsFragment", "Loaded " + eventTypes.size() + " event types");
                }
            }

            @Override
            public void onFailure(Call<Collection<GetEventTypeResponse>> call, Throwable t) {
                Log.e("AllProductsFragment", "Failed to load event types", t);
            }
        });
    }

    private void setupRecyclerView(View rootView) {
        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view_events);
        adapter = new AllEventsAdapter(getContext(), events);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        adapter.setOnEventClickListener(this);
    }

    private void setupSearchView(View rootView) {
        SearchView searchView = rootView.findViewById(R.id.search_bar);
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    searchQuery = query.trim().isEmpty() ? null : query.trim();
                    currentPage = 0;
                    loadEvents();
                    return true;
                }
                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });
        }
    }

    private void setupCreateEventFab(View rootView) {
        FloatingActionButton fabCreateEvent = rootView.findViewById(R.id.fab_create_event);

        if (AuthUtils.getToken(requireContext()) != null &&
                AuthUtils.getUserRoles(requireContext()).contains(UserRoles.EventOrganizer)) {
            fabCreateEvent.setVisibility(View.VISIBLE);
            fabCreateEvent.setOnClickListener(v -> {
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_all_events_to_event);
            });
        } else {
            fabCreateEvent.setVisibility(View.GONE);
        }
    }

    // 🔹 Load events with all filters
    private void loadEvents() {
        Call<PagedResponse<GetEventResponse>> call =
                eventService.filterEvents(
                        currentPage,
                        pageSize,
                        searchQuery,
                        selectedEventTypeId,
                        minParticipants,
                        maxParticipants,
                        startDate,
                        endDate,
                        sort
                );

        call.enqueue(new Callback<PagedResponse<GetEventResponse>>() {
            @Override
            public void onResponse(@NonNull Call<PagedResponse<GetEventResponse>> call,
                                   @NonNull Response<PagedResponse<GetEventResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PagedResponse<GetEventResponse> pageResponse = response.body();

                    events.clear();
                    events.addAll(pageResponse.getContent());
                    adapter.updateEvents(events);

                    totalPages = pageResponse.getTotalPages();
                    textCurrentPage.setText("Page " + (currentPage + 1) + " of " + totalPages);

                    Log.d("AllEventsFragment", "Loaded " + events.size() + " events");
                } else {
                    Log.e("AllEventsFragment", "Failed to load events: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<PagedResponse<GetEventResponse>> call, @NonNull Throwable t) {
                Log.e("AllEventsFragment", "Failed to load events", t);
            }
        });
    }

    // 🔹 Clear filters
    private void clearFilters() {
        searchQuery = null;
        selectedEventTypeId = null;
        minParticipants = null;
        maxParticipants = null;
        startDate = null;
        endDate = null;
        sort = null;

        currentPage = 0;
        loadEvents();

        Toast.makeText(getContext(), "Filters cleared", Toast.LENGTH_SHORT).show();
    }

    // ---- Filter dialog ----
    private void showFilterDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(getActivity());
        View dialogView = getLayoutInflater().inflate(R.layout.bottom_sheet_filter_events, null);

        // Event types
        RadioGroup radioGroupEventTypes = dialogView.findViewById(R.id.radio_group_event_types);
        if (radioGroupEventTypes != null) {
            RadioButton allButton = new RadioButton(getActivity());
            allButton.setText("All Types");
            allButton.setTag(null);
            allButton.setChecked(selectedEventTypeId == null);
            radioGroupEventTypes.addView(allButton);

            for (GetEventTypeResponse type : eventTypes) {
                RadioButton rb = new RadioButton(getActivity());
                rb.setText(type.getName());
                rb.setTag(type.getId());
                rb.setChecked(selectedEventTypeId != null && selectedEventTypeId.equals(type.getId()));
                radioGroupEventTypes.addView(rb);
            }
        }

        // Restore participants
        EditText minParticipantsInput = dialogView.findViewById(R.id.editText_min_participants);
        if (minParticipants != null) minParticipantsInput.setText(String.valueOf(minParticipants));

        EditText maxParticipantsInput = dialogView.findViewById(R.id.editText_max_participants);
        if (maxParticipants != null) maxParticipantsInput.setText(String.valueOf(maxParticipants));

        // Restore dates
        EditText startDateInput = dialogView.findViewById(R.id.editText_start_date);
        EditText endDateInput = dialogView.findViewById(R.id.editText_end_date);

        // Restore saved dates
        if (startDate != null) startDateInput.setText(startDate.toString());
        if (endDate != null) endDateInput.setText(endDate.toString());

// Date picker for start date
        startDateInput.setOnClickListener(v -> {
            showDatePicker((date) -> startDateInput.setText(date));
        });
// Date picker for end date
        endDateInput.setOnClickListener(v -> {
            showDatePicker((date) -> endDateInput.setText(date));
        });

        startDateInput.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2; // drawableEnd
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (startDateInput.getRight() - startDateInput.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    startDateInput.setText(""); // clear the date
                    return true;
                }
            }
            return false;
        });
        endDateInput.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (endDateInput.getRight() - endDateInput.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    endDateInput.setText("");
                    return true;
                }
            }
            return false;
        });


        // Restore sorting
        RadioGroup sortGroup = dialogView.findViewById(R.id.radio_group_sort);
        if ("startDate,asc".equals(sort)) {
            sortGroup.check(R.id.radio_button_closest);
        } else if ("startDate,desc".equals(sort)) {
            sortGroup.check(R.id.radio_button_furthest);
        } else {
            sortGroup.check(R.id.radio_button_normal);
        }

        // Apply
        Button applyButton = dialogView.findViewById(R.id.button_apply_filters);
        applyButton.setOnClickListener(v -> {
            applyFilters(dialogView);
            dialog.dismiss();
        });

        Button cancelButton = dialogView.findViewById(R.id.button_cancel_filters);
        cancelButton.setOnClickListener(v -> dialog.dismiss());



        dialog.setContentView(dialogView);
        dialog.show();
    }

    private void applyFilters(View dialogView) {
        // Event type
        RadioGroup typeGroup = dialogView.findViewById(R.id.radio_group_event_types);
        if (typeGroup != null) {
            int selectedId = typeGroup.getCheckedRadioButtonId();
            if (selectedId != -1) {
                RadioButton selectedRadio = dialogView.findViewById(selectedId);
                selectedEventTypeId = (Long) selectedRadio.getTag();
            }
        }

        // Participants
        EditText minInput = dialogView.findViewById(R.id.editText_min_participants);
        String minText = minInput.getText().toString().trim();
        minParticipants = minText.isEmpty() ? null : Integer.parseInt(minText);

        EditText maxInput = dialogView.findViewById(R.id.editText_max_participants);
        String maxText = maxInput.getText().toString().trim();
        maxParticipants = maxText.isEmpty() ? null : Integer.parseInt(maxText);

        // Dates
        EditText startDateInput = dialogView.findViewById(R.id.editText_start_date);
        EditText endDateInput = dialogView.findViewById(R.id.editText_end_date);

        startDate = startDateInput.getText().toString().trim().isEmpty() ? null : LocalDate.parse(startDateInput.getText().toString().trim());
        endDate = endDateInput.getText().toString().trim().isEmpty() ? null : LocalDate.parse(endDateInput.getText().toString().trim());


        // Sorting
        RadioGroup sortGroup = dialogView.findViewById(R.id.radio_group_sort);
        int sortId = sortGroup.getCheckedRadioButtonId();
        if (sortId == R.id.radio_button_closest) {
            sort = "startDate,asc";
        } else if (sortId == R.id.radio_button_furthest) {
            sort = "startDate,desc";
        } else {
            sort = null; // normal
        }

        currentPage = 0;
        loadEvents();
    }

    // ---- OnEventClick ----
    @Override
    public void onEventClick(GetEventResponse event) {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.fragment_nav_content_main);
        Bundle args = new Bundle();
        args.putLong("eventId", event.getId());
        navController.navigate(R.id.action_all_events_to_event, args);
    }

    private void setupPagination(View rootView) {
        textCurrentPage = rootView.findViewById(R.id.text_current_page);
        editPageNumber = rootView.findViewById(R.id.edit_page_number);
        spinnerPageSize = rootView.findViewById(R.id.spinner_page_size);

        ArrayAdapter<CharSequence> adapterSpinner = ArrayAdapter.createFromResource(
                getContext(),
                R.array.page_size_options,
                android.R.layout.simple_spinner_item);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPageSize.setAdapter(adapterSpinner);
        spinnerPageSize.setSelection(1); // default = 10

        spinnerPageSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int newSize = Integer.parseInt(parent.getItemAtPosition(position).toString());
                if (pageSize != newSize) {
                    pageSize = newSize;
                    currentPage = 0;
                    loadEvents();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        rootView.findViewById(R.id.button_first_page).setOnClickListener(v -> {
            currentPage = 0;
            loadEvents();
        });
        rootView.findViewById(R.id.button_prev_page).setOnClickListener(v -> {
            if (currentPage > 0) {
                currentPage--;
                loadEvents();
            }
        });
        rootView.findViewById(R.id.button_next_page).setOnClickListener(v -> {
            if (currentPage + 1 < totalPages) {
                currentPage++;
                loadEvents();
            }
        });
        rootView.findViewById(R.id.button_last_page).setOnClickListener(v -> {
            if (totalPages > 0) {
                currentPage = totalPages - 1;
                loadEvents();
            }
        });
        rootView.findViewById(R.id.button_go_page).setOnClickListener(v -> {
            String input = editPageNumber.getText().toString();
            if (!input.isEmpty()) {
                int page = Integer.parseInt(input) - 1;
                if (page >= 0 && page < totalPages) {
                    currentPage = page;
                    loadEvents();
                } else {
                    Toast.makeText(getContext(), "Invalid page number", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showDatePicker(OnDateSelectedListener listener) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                getContext(),
                (view, year1, month1, dayOfMonth) -> {
                    // Format to yyyy-MM-dd
                    String date = String.format(Locale.US, "%04d-%02d-%02d", year1, month1 + 1, dayOfMonth);
                    listener.onDateSelected(date);
                },
                year, month, day
        );
        dialog.show();
    }

    interface OnDateSelectedListener {
        void onDateSelected(String date);
    }

}
