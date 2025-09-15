package com.eventplanner.fragments.solutions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.eventplanner.R;
import com.eventplanner.adapters.solutions.SolutionListAdapter;
import com.eventplanner.model.responses.PagedResponse;
import com.eventplanner.model.responses.solutions.GetSolutionResponse;
import com.eventplanner.services.SolutionService;
import com.eventplanner.utils.HttpUtils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TopSolutionsFragment extends Fragment {

    private SolutionService solutionService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        HttpUtils.initialize(getContext());

        View rootView = inflater.inflate(R.layout.fragment_top_solutions, container, false);
        solutionService = HttpUtils.getSolutionService();

        loadTopSolutions(rootView);

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

    private void loadTopSolutions(View rootView) {
        solutionService.getTopSolutions().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<PagedResponse<GetSolutionResponse>> call,
                                   @NonNull Response<PagedResponse<GetSolutionResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<GetSolutionResponse> solutions = response.body().getContent();
                    setupSolutionList(rootView, solutions);
                }
            }

            @Override
            public void onFailure(@NonNull Call<PagedResponse<GetSolutionResponse>> call,
                                  @NonNull Throwable t) {
                // Handle error
            }
        });
    }

    private void setupSolutionList(View rootView, List<GetSolutionResponse> solutions) {
        ListView listView = rootView.findViewById(android.R.id.list);
        SolutionListAdapter adapter = new SolutionListAdapter(requireContext(), solutions);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            GetSolutionResponse solution = (GetSolutionResponse) parent.getItemAtPosition(position);
            navigateToSolutionDetails(solution.getId());
        });
    }

    private void navigateToSolutionDetails(long solutionId) {
        NavController navController = Navigation.findNavController(requireActivity(),
                R.id.fragment_nav_content_main);
        Bundle args = new Bundle();
        args.putString("solutionId", String.valueOf(solutionId));
        navController.navigate(R.id.action_home_to_solution_details, args);
    }

    private void navigateToAllEvents() {
        NavController navController = Navigation.findNavController(requireActivity(),
                R.id.fragment_nav_content_main);
        navController.navigate(R.id.action_home_to_all_events);
    }
}
