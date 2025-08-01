package com.eventplanner.fragments.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.eventplanner.adapters.activities.ActivitiesAdapter;
import com.eventplanner.databinding.FragmentActivitiesDialogBinding;
import com.eventplanner.model.requests.activities.CreateActivityRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ActivitiesDialogFragment extends DialogFragment {
    private FragmentActivitiesDialogBinding binding;
    private ActivitiesAdapter adapter;
    private List<CreateActivityRequest> activities = new ArrayList<>();
    private boolean isReadOnly;

    public static ActivitiesDialogFragment newInstance(List<CreateActivityRequest> activities, boolean isReadOnly) {
        ActivitiesDialogFragment fragment = new ActivitiesDialogFragment();
        Bundle args = new Bundle();
        Gson gson = new Gson();
        args.putString("activities", gson.toJson(activities));
        args.putBoolean("isReadOnly", isReadOnly);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Gson gson = new Gson();
            String activitiesJson = getArguments().getString("activities");
            Type type = new TypeToken<List<CreateActivityRequest>>(){}.getType();
            activities = gson.fromJson(activitiesJson, type);
            isReadOnly = getArguments().getBoolean("isReadOnly");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentActivitiesDialogBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set title
        binding.dialogTitle.setText("Manage Activities");

        // Setup RecyclerView
        adapter = new ActivitiesAdapter(activities, isReadOnly);
        binding.activitiesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.activitiesRecyclerView.setAdapter(adapter);

        // Setup buttons
        binding.cancelButton.setOnClickListener(v -> dismiss());
        binding.saveButton.setOnClickListener(v -> saveActivities());
        binding.addButton.setOnClickListener(v -> addActivity());

        // Hide add button if read-only
        if (isReadOnly) {
            binding.addButton.setVisibility(View.GONE);
            binding.saveButton.setVisibility(View.GONE);
        }
    }

    private void addActivity() {
        activities.add(new CreateActivityRequest("", "", LocalDateTime.now(), LocalDateTime.now()));
        adapter.notifyItemInserted(activities.size() - 1);
    }

    private void saveActivities() {
        if (validateActivities()) {
            Bundle result = new Bundle();
            Gson gson = new Gson();
            result.putString("activities", gson.toJson(activities));
            getParentFragmentManager().setFragmentResult("activities_request", result);
            dismiss();
        }
    }

    private boolean validateActivities() {
        for (CreateActivityRequest activity : activities) {
            if (activity.getName().isEmpty()) {
                Toast.makeText(requireContext(), "Activity name is required", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (activity.getStartTime() == null) {
                Toast.makeText(requireContext(), "Start time is required", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (activity.getEndTime() == null) {
                Toast.makeText(requireContext(), "End time is required", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
