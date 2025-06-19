package com.eventplanner.fragments.solutions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.eventplanner.R;
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
        return new TopSolutionsFragment();
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

        // on click navigate to SolutionDetailsFragment
        adapter.setOnItemClickListener(solution -> {
            Bundle bundle = new Bundle();
            bundle.putString("solutionId", String.valueOf(2)); //TODO: srediti

            NavController navController = Navigation.findNavController(rootView);
            navController.navigate(R.id.action_home_to_solutionDetails, bundle);
        });

        NavController navController = Navigation.findNavController(getActivity(), R.id.fragment_nav_content_main);

        Button browseServicesButton = rootView.findViewById(R.id.browse_services_button);
        browseServicesButton.setOnClickListener(v -> {
            navController.navigate(R.id.nav_all_services);
        });
        Button browseProductsButton = rootView.findViewById(R.id.browse_products_button);
        browseProductsButton.setOnClickListener(v -> {
            navController.navigate(R.id.nav_all_products);
        });

        return rootView;
    }
}
