package com.eventplanner.fragments.solutions;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.eventplanner.R;
import com.eventplanner.adapters.solutions.SolutionListAdapter;
import com.eventplanner.model.solutions.Solution;

import java.util.ArrayList;
import java.util.List;

public class TopSolutionsFragment extends Fragment {
    public TopSolutionsFragment() {
        // Required empty public constructor
    }
    public static TopSolutionsFragment newInstance() {
        TopSolutionsFragment fragment = new TopSolutionsFragment();
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_top_solutions, container, false);

        ListView listView = rootView.findViewById(android.R.id.list);

        List<Solution> solutions = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            solutions.add(new Solution("Solution " + (i + 1), "Description for solution " + (i + 1)));
        }
        SolutionListAdapter adapter = new SolutionListAdapter(getContext(), solutions);
        listView.setAdapter(adapter);

        return rootView;
    }
}
