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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.eventplanner.R;
import com.eventplanner.model.constants.Constants;
import com.eventplanner.model.requests.eventTypes.CreateEventTypeRequest;
import com.eventplanner.model.requests.eventTypes.UpdateEventTypeRequest;
import com.eventplanner.model.responses.eventTypes.GetEventTypeResponse;
import com.eventplanner.model.responses.solutionCateogries.GetSolutionCategoryResponse;
import com.eventplanner.services.EventTypeService;
import com.eventplanner.services.SolutionCategoryService;
import com.eventplanner.utils.HttpUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventTypeFragment extends Fragment {
    private GetEventTypeResponse eventType;
    private List<GetSolutionCategoryResponse> categories;
    private EditText nameEditText;
    private EditText descriptionEditText;
    private LinearLayout categoriesContainer;
    private TextView categoriesLabel;
    private Button submitButton;
    private final Set<Long> selectedCategoryIds = new HashSet<>();
    private boolean isEditMode = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_type, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup views
        TextView titleTextView = view.findViewById(R.id.titleTextView);
        nameEditText = view.findViewById(R.id.nameEditText);
        descriptionEditText = view.findViewById(R.id.descriptionEditText);
        categoriesContainer = view.findViewById(R.id.categoriesContainer);
        categoriesLabel = view.findViewById(R.id.categoriesLabel);
        Button cancelButton = view.findViewById(R.id.cancelButton);
        submitButton = view.findViewById(R.id.submitButton);

        // Determine if in edit mode
        long eventTypeId = getArguments() != null ? getArguments().getLong("eventTypeId", Constants.NullId) : Constants.NullId;
        isEditMode = eventTypeId != Constants.NullId;

        cancelButton.setOnClickListener(v -> NavHostFragment.findNavController(EventTypeFragment.this).navigateUp());

        titleTextView.setText(isEditMode ? getString(R.string.edit_event_type) : getString(R.string.create_event_type));
        nameEditText.setEnabled(!isEditMode);

        descriptionEditText.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateForm();
            }

            public void afterTextChanged(Editable s) {
            }
        });

        submitButton.setOnClickListener(v -> handleSubmit());

        if (isEditMode) {
            fetchEventType(eventTypeId);
        } else {
            fetchAcceptedCategories(null);
        }
    }

    private void fetchEventType(long id) {
        EventTypeService service = HttpUtils.getEventTypeService();
        service.getEventTypeById(id).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<GetEventTypeResponse> call, @NonNull Response<GetEventTypeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    eventType = response.body();
                    nameEditText.setText(eventType.getName());
                    descriptionEditText.setText(eventType.getDescription());
                    fetchAcceptedCategories(eventType.getRecommendedSolutionCategories());
                } else {
                    Toast.makeText(getContext(), "Failed to load event type", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<GetEventTypeResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Error loading event type", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchAcceptedCategories(@Nullable Collection<GetSolutionCategoryResponse> preselected) {
        SolutionCategoryService service = HttpUtils.getSolutionCategoryService();
        service.getAcceptedCategories().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Collection<GetSolutionCategoryResponse>> call, @NonNull Response<Collection<GetSolutionCategoryResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categories = new ArrayList<>(response.body());

                    if (preselected != null) {
                        for (GetSolutionCategoryResponse cat : preselected) {
                            selectedCategoryIds.add(cat.getId());
                        }
                    }

                    setupCategoryCheckboxes();
                    validateForm();
                } else {
                    Toast.makeText(getContext(), "Failed to load categories", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Collection<GetSolutionCategoryResponse>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Error loading categories", Toast.LENGTH_SHORT).show();
            }
        });
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

            if (selectedCategoryIds.contains(category.getId())) {
                checkBox.setChecked(true);
            }

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                Long id = (Long) buttonView.getTag();
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
        boolean descriptionChanged;
        boolean categoriesChanged;
        boolean isValid;

        if (eventType != null) {
            descriptionChanged = !descriptionEditText.getText().toString().equals(eventType.getDescription());
            categoriesChanged = hasCategoriesChanged();
            isValid = descriptionChanged || categoriesChanged;
        } else {
            isValid = !nameEditText.getText().toString().trim().isEmpty() && !descriptionEditText.getText().toString().trim().isEmpty();
        }

        submitButton.setEnabled(isValid);
        return isValid;
    }

    private boolean hasCategoriesChanged() {
        if (eventType == null || eventType.getRecommendedSolutionCategories() == null) {
            return false;
        }

        Set<Long> initialIds = eventType.getRecommendedSolutionCategories().stream().map(GetSolutionCategoryResponse::getId).collect(Collectors.toSet());

        return !initialIds.equals(selectedCategoryIds);
    }

    private void handleSubmit() {
        if (!validateForm()) {
            return;
        }

        String name = nameEditText.getText().toString();
        String description = descriptionEditText.getText().toString();

        List<GetSolutionCategoryResponse> selectedCategories = categories != null ? categories.stream().filter(cat -> selectedCategoryIds.contains(cat.getId())).collect(Collectors.toList()) : new ArrayList<>();

        List<Long> selectedCategoryIdsList = selectedCategories.stream().map(GetSolutionCategoryResponse::getId).collect(Collectors.toList());

        EventTypeService service = HttpUtils.getEventTypeService();

        if (isEditMode) {
            UpdateEventTypeRequest update = new UpdateEventTypeRequest();
            update.setDescription(description);
            update.setRecommendedSolutionCategoryIds(selectedCategoryIdsList);

            service.updateEventType(eventType.getId(), update).enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    Toast.makeText(getContext(), response.isSuccessful() ? "Event type updated" : "Update failed", Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(EventTypeFragment.this).navigateUp();
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    Toast.makeText(getContext(), "Update error", Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            CreateEventTypeRequest create = new CreateEventTypeRequest();
            create.setName(name);
            create.setDescription(description);
            create.setRecommendedSolutionCategoryIds(selectedCategoryIdsList);

            service.createEventType(create).enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<GetEventTypeResponse> call, @NonNull Response<GetEventTypeResponse> response) {
                    Toast.makeText(getContext(), response.isSuccessful() ? "Event type created" : "Creation failed", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(@NonNull Call<GetEventTypeResponse> call, @NonNull Throwable t) {
                    Toast.makeText(getContext(), "Failed to create event type", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
