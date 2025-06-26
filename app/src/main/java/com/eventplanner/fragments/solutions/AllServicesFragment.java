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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.eventplanner.R;
import com.eventplanner.adapters.solutions.SolutionListAdapter;
import com.eventplanner.model.solutions.ReservationType;
import com.eventplanner.model.solutions.Service;
import com.eventplanner.model.solutions.Solution;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

public class AllServicesFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the fragment layout
        View rootView = inflater.inflate(R.layout.fragment_all_services, container, false);

        populateList(rootView);

        // Set up the filter button
        Button filterButton = rootView.findViewById(R.id.button_filter_services);
        filterButton.setOnClickListener(v -> {
            Log.i("AllServicesFragment", "Filter button clicked");
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity());
            View dialogView = getLayoutInflater().inflate(R.layout.bottom_sheet_filter_services, null);

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
            solutions.add(new Service((i + 1) + ". Service", "Description for Service " + (i + 1), i * 1000, 0,
                    "Specifics for Service", i, i, i, ReservationType.AUTOMATIC));
        }
        SolutionListAdapter adapter = new SolutionListAdapter(getContext(), solutions);
        listView.setAdapter(adapter);

        // on click navigate to SolutionDetailsFragment
        adapter.setOnItemClickListener(solution -> {
            Bundle bundle = new Bundle();
            bundle.putString("solutionId", String.valueOf(1)); //TODO: srediti treba proslediti id solutiona

            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_allServices_to_solutionDetails, bundle);
        });
    }
}
