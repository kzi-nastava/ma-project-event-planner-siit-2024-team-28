package com.eventplanner.fragments.users;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eventplanner.R;


public class BusinessOwnerDetailsFragment extends Fragment {

    private String mParam1;
    private String mParam2;

    public BusinessOwnerDetailsFragment() {
        // Required empty public constructor
    }

    public static BusinessOwnerDetailsFragment newInstance(String param1, String param2) {
        BusinessOwnerDetailsFragment fragment = new BusinessOwnerDetailsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_business_owner_details, container, false);
    }
}