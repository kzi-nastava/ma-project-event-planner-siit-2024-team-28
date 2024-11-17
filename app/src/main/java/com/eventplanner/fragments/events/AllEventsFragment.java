package com.eventplanner.fragments.events;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.eventplanner.R;
import com.eventplanner.adapters.events.EventListAdapter;
import com.eventplanner.model.events.Event;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

public class AllEventsFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for the fragment
        View rootView = inflater.inflate(R.layout.fragment_all_events, container, false);

        populateList(rootView);

        // Set up the filter button to show a bottom sheet dialog
        Button filterButton = rootView.findViewById(R.id.button_filter_events);
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("AllEventsFragment", "Filter button clicked");

                // Create and show the bottom sheet dialog
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
                View dialogView = getLayoutInflater().inflate(R.layout.bottom_sheet_filter_events, null);

                // Populate the dialog with event type checkboxes
                String[] eventTypes = getResources().getStringArray(R.array.event_types);
                LinearLayout eventTypesCheckboxes = dialogView.findViewById(R.id.event_types_checkboxes);
                for (String eventType : eventTypes) {
                    CheckBox checkBox = new CheckBox(getContext());
                    checkBox.setText(eventType);
                    checkBox.setButtonTintList(getResources().getColorStateList(R.color.cool_purple));
                    eventTypesCheckboxes.addView(checkBox);
                }

                // Set the dialog content view and show it
                bottomSheetDialog.setContentView(dialogView);
                bottomSheetDialog.show();
            }
        });

        return rootView;
    }

    private void populateList(View rootView) {
        ListView listView = rootView.findViewById(android.R.id.list);
        List<Event> events = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            events.add(new Event("Event " + (i+1), "Description for event"));
        }
        EventListAdapter adapter = new EventListAdapter(getContext(), events);
        listView.setAdapter(adapter);
    }
}
