package com.eventplanner.fragments.services;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.eventplanner.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class ServicesOverviewFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_services_overview, container, false);

        // Apply window insets to handle system bars
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set up add services button to navigate to ServiceCreationActivity
        Button addServicesButton = view.findViewById(R.id.button_add_service);
        addServicesButton.setOnClickListener(v -> {
            Log.i("ServicesOverviewFragment", "Add services button clicked");
            Intent intent = new Intent(getActivity(), ServiceCreationFragment.class);
            startActivity(intent);
        });

        // Set up filter button to show BottomSheetDialog
        Button filterButton = view.findViewById(R.id.button_filter_services);
        filterButton.setOnClickListener(v -> {
            Log.i("ServicesOverviewFragment", "Filter button clicked");
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
            View dialogView = getLayoutInflater().inflate(R.layout.bottom_sheet_filter_services, null);

            // Populate the BottomSheetDialog with RadioButtons
            String[] eventTypes = getResources().getStringArray(R.array.event_types);
            RadioGroup radioGroupCategories = dialogView.findViewById(R.id.radio_group_categories);
            for (String eventType : eventTypes) {
                RadioButton radioButton = new RadioButton(getContext());
                radioButton.setText(eventType);
                radioButton.setButtonTintList(getResources().getColorStateList(R.color.cool_purple));
                radioGroupCategories.addView(radioButton);
            }

            // Populate the BottomSheetDialog with CheckBoxes
            LinearLayout eventTypesCheckboxes = dialogView.findViewById(R.id.event_types_checkboxes);
            for (String eventType : eventTypes) {
                CheckBox checkBox = new CheckBox(getContext());
                checkBox.setText(eventType);
                checkBox.setButtonTintList(getResources().getColorStateList(R.color.cool_purple));
                eventTypesCheckboxes.addView(checkBox);
            }

            bottomSheetDialog.setContentView(dialogView);
            bottomSheetDialog.show();
        });

        // Inflate and add service cards to the container
        LinearLayout linearLayoutContainer = view.findViewById(R.id.cards_container);
        for (int i = 0; i < 5; i++) {
            View itemView = inflater.inflate(R.layout.service_card, linearLayoutContainer, false);
            ImageButton imageButton = itemView.findViewById(R.id.edit_button);
            imageButton.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), ServiceCreationFragment.class);
                startActivity(intent);
            });
            linearLayoutContainer.addView(itemView);
        }

        return view;
    }
}
