package com.eventplanner.fragments.services;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.eventplanner.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class ServiceCreationFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_service_creation, container, false);

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Load ChipGroup and populate it with event types
        ChipGroup eventTypesChips = view.findViewById(R.id.chip_group_event_types);
        String[] eventTypes = getResources().getStringArray(R.array.event_types);
        for (String event : eventTypes) {
            Chip chip = new Chip(getContext());
            chip.setText(event);
            chip.setCheckable(true);
            chip.setCheckedIconVisible(false);
            chip.setChipBackgroundColorResource(android.R.color.darker_gray);

            // Set listener to change chip background color when selected
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    chip.setChipBackgroundColorResource(android.R.color.holo_purple);
                } else {
                    chip.setChipBackgroundColorResource(android.R.color.darker_gray);
                }
            });
            eventTypesChips.addView(chip);
        }

        // Set up EditText views and RadioGroup for duration options
        EditText editTextFixedDuration = view.findViewById(R.id.editText_fixed_duration);
        EditText editTextMinDuration = view.findViewById(R.id.editText_min_duration);
        EditText editTextMaxDuration = view.findViewById(R.id.editText_max_duration);
        editTextFixedDuration.setEnabled(false);
        editTextMinDuration.setEnabled(false);
        editTextMaxDuration.setEnabled(false);

        RadioGroup radioGroupDuration = view.findViewById(R.id.radio_group_duration);
        radioGroupDuration.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selectedRadioButton = view.findViewById(checkedId);
            String selectedText = selectedRadioButton.getText().toString();

            // Log selection
            Log.d("RadioGroup", "You chose RadioButton: " + selectedText);

            // Enable or disable EditText views based on selection
            if (selectedText.equals(getString(R.string.fixed))) {
                editTextFixedDuration.setEnabled(true);
                editTextMinDuration.setEnabled(false);
                editTextMaxDuration.setEnabled(false);
            } else if (selectedText.equals(getString(R.string.min_max))) {
                editTextFixedDuration.setEnabled(false);
                editTextMinDuration.setEnabled(true);
                editTextMaxDuration.setEnabled(true);
            }
        });

        return view;
    }
}
