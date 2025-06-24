package com.eventplanner.fragments.solutions;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.eventplanner.R;
import com.eventplanner.model.responses.solutions.GetSolutionResponse;
import com.eventplanner.services.SolutionService;
import com.eventplanner.utils.HttpUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SolutionDetailsFragment extends Fragment {
    private SolutionService solutionService;

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
        solutionService = HttpUtils.getSolutionService();
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            solutionId = getArguments().getString(ARG_SOLUTION_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate layout
        View rootView = inflater.inflate(R.layout.fragment_solution_details, container, false);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView solutionTitle = view.findViewById(R.id.textTitle);

        Long id = Long.parseLong(solutionId);
        Call<GetSolutionResponse> call = solutionService.getSolutionById(id);


        call.enqueue(new Callback<GetSolutionResponse>() {
            @Override
            public void onResponse(Call<GetSolutionResponse> call, Response<GetSolutionResponse> response) {
                if (response.isSuccessful()) {
                    GetSolutionResponse solution = response.body();
                    if (solution != null) {
                        // UI postavljanje
                        solutionTitle.setText(solution.getName());
                    }
                } else {
                    Log.e("SolutionDetailsFragment", "Response error code: " + response.code());
                    showErrorDialog();
                }
            }

            @Override
            public void onFailure(Call<GetSolutionResponse> call, Throwable t) {
                Log.e("SolutionDetailsFragment", "Network failure", t);
                showErrorDialog();
            }
        });
    }

    private void showErrorDialog() {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setMessage("There has been an error.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }
}