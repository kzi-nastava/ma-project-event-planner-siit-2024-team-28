package com.eventplanner.fragments.solutions;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.eventplanner.R;

public class SolutionDetailsFragment extends Fragment {

    private static final String ARG_SOLUTION_ID = "solutionId";

    private String solutionId;

    public static SolutionDetailsFragment newInstance(String solutionId) {
        SolutionDetailsFragment fragment = new SolutionDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SOLUTION_ID, solutionId);
        fragment.setArguments(args);
        return fragment;
    }

    public SolutionDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            solutionId = getArguments().getString(ARG_SOLUTION_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate layout
        View view = inflater.inflate(R.layout.fragment_solution_details, container, false);

        TextView soltuionTitle = view.findViewById(R.id.textTitle);
        soltuionTitle.setText(solutionId);


        return view;
    }
}