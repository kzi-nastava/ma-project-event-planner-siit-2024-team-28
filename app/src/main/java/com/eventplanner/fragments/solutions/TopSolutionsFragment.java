package com.eventplanner.fragments.solutions;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.eventplanner.R;
import com.eventplanner.activities.events.AllEventsActivity;
import com.eventplanner.activities.solutions.AllProductsActivity;
import com.eventplanner.activities.solutions.AllServicesActivity;
import com.eventplanner.adapters.solutions.SolutionListAdapter;
import com.eventplanner.model.solutions.Product;
import com.eventplanner.model.solutions.ReservationType;
import com.eventplanner.model.solutions.Service;
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
            if(i%2==0) {
                solutions.add(new Product((i + 1) + ". Product", "Description for Product " + (i + 1), i * 1000, 0));
            }
            else {
                solutions.add(new Service((i + 1) + ". Service", "Description for Service " + (i + 1), i * 1000, 0,
                        "Specifics for Service", i, i, i, ReservationType.AUTOMATIC));
            }
        }
        SolutionListAdapter adapter = new SolutionListAdapter(getContext(), solutions);
        listView.setAdapter(adapter);

        Button browseServicesButton = rootView.findViewById(R.id.browse_services_button);
        browseServicesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AllServicesActivity.class);
                startActivity(intent);
            }
        });
        Button browseProductsButton = rootView.findViewById(R.id.browse_products_button);
        browseProductsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AllProductsActivity.class);
                startActivity(intent);
            }
        });

        return rootView;
    }
}
