package com.eventplanner.fragments.events;

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
import com.eventplanner.adapters.events.EventListAdapter;
import com.eventplanner.model.events.Event;

import java.util.ArrayList;
import java.util.List;

public class TopEventsFragment extends Fragment {
    public TopEventsFragment() {
        // Required empty public constructor
    }
    public static TopEventsFragment newInstance() {
        TopEventsFragment fragment = new TopEventsFragment();
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_top_events, container, false);

        ListView listView = rootView.findViewById(android.R.id.list);

        List<Event> events = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            events.add(new Event((i + 1) + ". Event ", "Description for event"));
        }
        EventListAdapter adapter = new EventListAdapter(getContext(), events);
        listView.setAdapter(adapter);

        return rootView;
    }
}
