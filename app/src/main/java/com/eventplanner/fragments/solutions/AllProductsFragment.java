package com.eventplanner.fragments.solutions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.eventplanner.R;
import com.eventplanner.adapters.solutions.SolutionListAdapter;
import com.eventplanner.model.solutions.Product;
import com.eventplanner.model.solutions.Solution;

import java.util.ArrayList;
import java.util.List;

public class AllProductsFragment extends Fragment {
    public AllProductsFragment() {
        // Required empty public constructor
    }
    public static AllProductsFragment newInstance() {
        AllProductsFragment fragment = new AllProductsFragment();
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_all_products, container, false);

        ListView listView = rootView.findViewById(android.R.id.list);

        List<Solution> solutions = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            solutions.add(new Product((i + 1) + ". Product", "Description for Product " + (i + 1), i * 1000, 0));

        }
        SolutionListAdapter adapter = new SolutionListAdapter(getContext(), solutions);
        listView.setAdapter(adapter);


        return rootView;
    }
}
