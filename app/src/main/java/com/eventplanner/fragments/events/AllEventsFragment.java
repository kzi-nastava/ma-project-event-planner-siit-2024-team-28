package com.eventplanner.fragments.events;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;

import com.eventplanner.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class AllEventsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the fragment layout
        View rootView = inflater.inflate(R.layout.fragment_all_events, container, false);

        // Set up the filter button
        Button filterButton = rootView.findViewById(R.id.button_filter_events);
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("AllEventsFragment", "Filter button clicked");
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity());
                View dialogView = getLayoutInflater().inflate(R.layout.bottom_sheet_filter_events, null);

                // Filling filter dialog with checkboxes
                String[] eventTypes = getResources().getStringArray(R.array.event_types);
                LinearLayout eventTypesCheckboxes = dialogView.findViewById(R.id.event_types_checkboxes);
                for (String eventType : eventTypes) {
                    CheckBox checkBox = new CheckBox(getActivity());
                    checkBox.setText(eventType);
                    checkBox.setButtonTintList(getResources().getColorStateList(R.color.cool_purple));
                    eventTypesCheckboxes.addView(checkBox);
                }

                bottomSheetDialog.setContentView(dialogView);
                bottomSheetDialog.show();
            }
        });

        return rootView;
    }
}
