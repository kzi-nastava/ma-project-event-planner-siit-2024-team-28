package com.eventplanner.fragments.eventTypes;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.eventplanner.R;
import com.eventplanner.model.responses.eventTypes.GetEventTypeResponse;
import com.eventplanner.model.responses.solutionCateogries.GetSolutionCategoryResponse;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EventTypeFragment extends Fragment {

    public interface OnEventTypeSubmitListener {
        void onEventTypeSubmit(GetEventTypeResponse eventType);
        void onCancel();
    }

    private OnEventTypeSubmitListener listener;

    private static final String ARG_EVENT_TYPE = "arg_event_type";
    private static final String ARG_CATEGORIES = "arg_categories";

    private GetEventTypeResponse eventType;
    private List<GetSolutionCategoryResponse> categories;

    private EditText nameEditText;
    private EditText descriptionEditText;
    private LinearLayout categoriesContainer;
    private TextView categoriesLabel;
    private Button submitButton;

    private final Set<String> selectedCategoryIds = new HashSet<>();

    // Factory method to create fragment with data
    public static EventTypeFragment newInstance(
            @Nullable GetEventTypeResponse eventType,
            @NonNull ArrayList<GetSolutionCategoryResponse> categories) {
        EventTypeFragment fragment = new EventTypeFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_EVENT_TYPE, (Serializable) eventType);
        args.putSerializable(ARG_CATEGORIES, categories);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnEventTypeSubmitListener(OnEventTypeSubmitListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_type, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get arguments
        if (getArguments() != null) {
            eventType = (GetEventTypeResponse) getArguments().getSerializable(ARG_EVENT_TYPE);
            categories = (List<GetSolutionCategoryResponse>) getArguments().getSerializable(ARG_CATEGORIES);
        }

        // Find views
        TextView titleTextView = view.findViewById(R.id.titleTextView);
        nameEditText = view.findViewById(R.id.nameEditText);
        descriptionEditText = view.findViewById(R.id.descriptionEditText);
        categoriesContainer = view.findViewById(R.id.categoriesContainer);
        categoriesLabel = view.findViewById(R.id.categoriesLabel);
        Button cancelButton = view.findViewById(R.id.cancelButton);
        submitButton = view.findViewById(R.id.submitButton);

        // Setup title and editability
        boolean isEditMode = eventType != null;
        titleTextView.setText(isEditMode ? getString(R.string.edit_event_type) : getString(R.string.create_event_type));
        nameEditText.setEnabled(!isEditMode);

        // Fill form if editing
        if (isEditMode) {
            nameEditText.setText(eventType.getName());
            descriptionEditText.setText(eventType.getDescription());
            if (eventType.getRecommendedSolutionCategories() != null && !eventType.getRecommendedSolutionCategories().isEmpty()) {
                categoriesLabel.setVisibility(View.VISIBLE);
            }
        } else {
            categoriesLabel.setVisibility(View.GONE);
        }

        // Setup categories checkboxes
        setupCategoryCheckboxes();

        // Text change listeners for validation
        TextWatcher formWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start,int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start,int before, int count) {
                validateForm();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };
        descriptionEditText.addTextChangedListener(formWatcher);

        // Cancel button
        cancelButton.setOnClickListener(v -> {
            if (listener != null) listener.onCancel();
        });

        // Submit button
        submitButton.setOnClickListener(v -> {
            if (!validateForm()) return;

            GetEventTypeResponse newEventType = new GetEventTypeResponse();
            if (isEditMode) {
                newEventType.setId(eventType.getId());
                newEventType.setActive(eventType.getIsActive());
            } else {
                newEventType.setActive(true);
            }
            newEventType.setName(nameEditText.getText().toString());
            newEventType.setDescription(descriptionEditText.getText().toString());

            // Filter selected categories
            List<GetSolutionCategoryResponse> selectedCategories = new ArrayList<>();
            if (categories != null) {
                for (GetSolutionCategoryResponse cat : categories) {
                    if (selectedCategoryIds.contains(cat.getId())) {
                        selectedCategories.add(cat);
                    }
                }
            }
            newEventType.setRecommendedSolutionCategories(selectedCategories);

            if (listener != null) listener.onEventTypeSubmit(newEventType);
        });

        // Initial validation
        validateForm();
    }

    private void setupCategoryCheckboxes() {
        categoriesContainer.removeAllViews();

        if (categories == null || categories.isEmpty()) {
            categoriesLabel.setVisibility(View.GONE);
            return;
        }
        categoriesLabel.setVisibility(View.VISIBLE);

        for (GetSolutionCategoryResponse category : categories) {
            CheckBox checkBox = new CheckBox(requireContext());
            checkBox.setText(category.getName());
            checkBox.setTag(category.getId());

            // Pre-select if editing and category is recommended
            if (eventType != null && eventType.getRecommendedSolutionCategories() != null) {
                for (GetSolutionCategoryResponse recCat : eventType.getRecommendedSolutionCategories()) {
                    if (recCat.getId().equals(category.getId())) {
                        checkBox.setChecked(true);
                        selectedCategoryIds.add(category.getId().toString());
                        break;
                    }
                }
            }

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                String id = (String) buttonView.getTag();
                if (isChecked) {
                    selectedCategoryIds.add(id);
                } else {
                    selectedCategoryIds.remove(id);
                }
                validateForm();
            });

            categoriesContainer.addView(checkBox);
        }
    }

    private boolean validateForm() {
        boolean descriptionChanged = true;
        boolean categoriesChanged = true;
        boolean isValid = false;

        if (eventType != null) {
            // Editing mode: require description changed or categories changed
            descriptionChanged = !descriptionEditText.getText().toString().equals(eventType.getDescription());
            categoriesChanged = hasCategoriesChanged();
            isValid = descriptionChanged || categoriesChanged;
        } else {
            // Creation mode: require non-empty description and name
            isValid = !nameEditText.getText().toString().trim().isEmpty()
                    && !descriptionEditText.getText().toString().trim().isEmpty();
        }

        submitButton.setEnabled(isValid);
        return isValid;
    }

    private boolean hasCategoriesChanged() {
        if (eventType == null || eventType.getRecommendedSolutionCategories() == null) return false;
        Set<String> initialIds = new HashSet<>();
        for (GetSolutionCategoryResponse c : eventType.getRecommendedSolutionCategories()) {
            initialIds.add(c.getId().toString());
        }
        return !initialIds.equals(selectedCategoryIds);
    }
}
