package com.eventplanner.fragments.solutions;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.fragment.app.Fragment;

import com.eventplanner.R;
import com.eventplanner.adapters.solutions.SolutionListAdapter;
import com.eventplanner.model.solutions.Product;
import com.eventplanner.model.solutions.Solution;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

public class AllProductsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the fragment layout
        View rootView = inflater.inflate(R.layout.fragment_all_products, container, false);

        populateList(rootView);

        // Set up the filter button
        Button filterButton = rootView.findViewById(R.id.button_filter_products);
        filterButton.setOnClickListener(v -> {
            Log.i("AllProductsFragment", "Filter button clicked");
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity());
            View dialogView = getLayoutInflater().inflate(R.layout.bottom_sheet_filter_products, null);

            // Filling filter dialog with radio buttons
            String[] eventTypes = getResources().getStringArray(R.array.event_types);
            RadioGroup radioGroupCategories = dialogView.findViewById(R.id.radio_group_categories);
            for (String eventType : eventTypes) {
                RadioButton radioButton = new RadioButton(getActivity());
                radioButton.setText(eventType);
                radioButton.setButtonTintList(getResources().getColorStateList(R.color.cool_purple));
                radioGroupCategories.addView(radioButton);
            }

            // Filling filter dialog with checkboxes
            LinearLayout eventTypesCheckboxes = dialogView.findViewById(R.id.event_types_checkboxes);
            for (String eventType : eventTypes) {
                CheckBox checkBox = new CheckBox(getActivity());
                checkBox.setText(eventType);
                checkBox.setButtonTintList(getResources().getColorStateList(R.color.cool_purple));
                eventTypesCheckboxes.addView(checkBox);
            }

            bottomSheetDialog.setContentView(dialogView);
            bottomSheetDialog.show();
        });

        return rootView;
    }

    private void populateList(View rootView) {
        ListView listView = rootView.findViewById(android.R.id.list);

        List<Solution> solutions = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            solutions.add(new Product((i + 1) + ". Product", "Description for Product " + (i + 1), i * 1000, 0));

        }
        SolutionListAdapter adapter = new SolutionListAdapter(getContext(), solutions);
        listView.setAdapter(adapter);

    }
}
