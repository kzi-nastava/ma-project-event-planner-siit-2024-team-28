package com.eventplanner.fragments.events;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.eventplanner.R;
import com.eventplanner.adapters.events.EventListAdapter;
import com.eventplanner.model.events.Event;
import com.eventplanner.model.solutions.ReservationType;

import java.util.ArrayList;
import java.util.List;

public class AllEventsFragment extends Fragment {
    public AllEventsFragment() {
        // Required empty public constructor
    }
    public static AllEventsFragment newInstance() {
        AllEventsFragment fragment = new AllEventsFragment();
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_all_events, container, false);

        ListView listView = rootView.findViewById(android.R.id.list);

        List<Event> events = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            events.add(new Event("Event " + (i+1), "Description for event"));
        }
        EventListAdapter adapter = new EventListAdapter(getContext(), events);
        listView.setAdapter(adapter);

        return rootView;
    }
}
