package com.eventplanner.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.eventplanner.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class ServiceCreationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_service_creation);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        /*
         *  Loading in ChipGroup used for multiple selection
         *  Accessing the ChipGroup then creating Chips and filling the ChipGroup
         *  TODO: Create an adapter to do this, this shouldnt have any business here
         * */

        ChipGroup eventTypesChips = findViewById(R.id.chip_group_event_types);
        String[] eventTypes = getResources().getStringArray(R.array.event_types);
        for(String event : eventTypes)
        {
            Chip chip = new Chip(this);
            chip.setText(event);
            chip.setCheckable(true);
            chip.setCheckedIconVisible(false);
            chip.setChipBackgroundColorResource(android.R.color.darker_gray);

            /*
             * Listener that helps Chips look better;
             * TLDR: when selected Chip becomes purple gg
             * */

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    chip.setChipBackgroundColorResource(android.R.color.holo_purple);
                } else {
                    chip.setChipBackgroundColorResource(android.R.color.darker_gray);
                }
            });
            eventTypesChips.addView(chip);
        }

        /*
         * RadioGroup_Duration listener for making certain EditText views available/unavailable
         * Also making both EditTextViews unavailable
         * */
        EditText editTextFixedDuration = findViewById(R.id.editText_fixed_duration);
        EditText editTextMinDuration = findViewById(R.id.editText_min_duration);
        EditText editTextMaxDuration = findViewById(R.id.editText_max_duration);
        editTextFixedDuration.setEnabled(false);
        editTextMinDuration.setEnabled(false);
        editTextMaxDuration.setEnabled(false);
        RadioGroup radioGroupDuration = findViewById(R.id.radio_group_duration);
        radioGroupDuration.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton selectedRadioButton = findViewById(checkedId);
                String selectedText = selectedRadioButton.getText().toString();

                // log just for fun never done it
                Log.d("RadioGroup", "You chose RadioButton: " + selectedText);

                // if fixed selected then we make fixed text input available and minmax unavailable and vice versa
                if(selectedText.equals(getString(R.string.fixed)))
                {
                    editTextFixedDuration.setEnabled(true);
                    editTextMinDuration.setEnabled(false);
                    editTextMaxDuration.setEnabled(false);
                }
                else if(selectedText.equals(getString(R.string.min_max)))
                {
                    editTextFixedDuration.setEnabled(false);
                    editTextMinDuration.setEnabled(true);
                    editTextMaxDuration.setEnabled(true);
                }
            }
        });
    }
}