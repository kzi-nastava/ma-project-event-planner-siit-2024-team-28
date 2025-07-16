package com.eventplanner.fragments.eventTypes;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eventplanner.R;
import com.eventplanner.adapters.eventTypes.EventTypesAdapter;
import com.eventplanner.model.responses.eventTypes.GetEventTypeResponse;
import com.eventplanner.services.EventTypeService;
import com.eventplanner.utils.HttpUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventTypesFragment extends Fragment {
    private EventTypesAdapter adapter;
    private List<GetEventTypeResponse> allEventTypes = new ArrayList<>();
    private final List<GetEventTypeResponse> filteredList = new ArrayList<>();
    private EditText searchEdit;
    private ToggleButton toggleActive, toggleInactive;
    private EventTypeService eventTypeService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_types, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerEventTypes);
        searchEdit = view.findViewById(R.id.editSearch);
        toggleActive = view.findViewById(R.id.toggleActive);
        toggleInactive = view.findViewById(R.id.toggleInactive);
        Button btnAdd = view.findViewById(R.id.btnAddEventType);

        adapter = new EventTypesAdapter(filteredList, new EventTypesAdapter.ActionCallback() {
            @Override
            public void onActivateToggle(GetEventTypeResponse type) {
                onToggleActivate(type);
            }

            @Override
            public void onEdit(GetEventTypeResponse type) {
                onEditEventType(type);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        searchEdit.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterEventTypes();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void afterTextChanged(Editable s) {}
        });

        toggleActive.setOnCheckedChangeListener((buttonView, isChecked) -> filterEventTypes());
        toggleInactive.setOnCheckedChangeListener((buttonView, isChecked) -> filterEventTypes());

        btnAdd.setOnClickListener(v -> {
            Bundle bundle = new Bundle(); // no eventTypeId means "create"
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_eventTypes_to_eventTypeForm, bundle);
        });

        fetchEventTypes();
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        eventTypeService = HttpUtils.getEventTypeService();
    }

    private void fetchEventTypes() {
        eventTypeService.getAllEventTypes().enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Collection<GetEventTypeResponse>> call, Response<Collection<GetEventTypeResponse>> response) {
                if (response.isSuccessful()) {
                    allEventTypes = new ArrayList<>(response.body());
                    filterEventTypes();
                } else {
                    Toast.makeText(getContext(), "Failed to load event types", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Collection<GetEventTypeResponse>> call, Throwable t) {
                Toast.makeText(getContext(), "Error loading event types", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterEventTypes() {
        String searchTerm = searchEdit.getText().toString().toLowerCase();
        boolean showActive = toggleActive.isChecked();
        boolean showInactive = toggleInactive.isChecked();

        filteredList.clear();
        for (GetEventTypeResponse type : allEventTypes) {
            boolean matchesActive = (type.getIsActive() && showActive) || (!type.getIsActive() && showInactive);
            boolean matchesSearch = type.getName().toLowerCase().contains(searchTerm);

            if (matchesActive && matchesSearch) {
                filteredList.add(type);
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void onToggleActivate(GetEventTypeResponse type) {
        if (type.getIsActive()) {
            eventTypeService.deactivateEventType(type.getId()).enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    fetchEventTypes();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(getContext(), "Failed to deactivate", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            eventTypeService.activateEventType(type.getId()).enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    fetchEventTypes();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(getContext(), "Failed to activate", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void onEditEventType(GetEventTypeResponse type) {
        Bundle bundle = new Bundle();
        bundle.putString("eventTypeId", type.getId().toString()); // pass ID to load for editing

        NavHostFragment.findNavController(this)
                .navigate(R.id.action_eventTypes_to_eventTypeForm, bundle);
    }

}
