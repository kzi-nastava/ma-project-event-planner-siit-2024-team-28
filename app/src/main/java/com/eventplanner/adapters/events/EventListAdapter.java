package com.eventplanner.adapters.events;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.eventplanner.R;
import com.eventplanner.model.events.Event;

import java.util.List;

public class EventListAdapter extends ArrayAdapter<Event> {
    public EventListAdapter(Context context, List<Event> events) {
        super(context, 0, events);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.event_card, parent, false);
        }

        Event event = getItem(position);

        TextView titleTextView = convertView.findViewById(R.id.event_name);
        TextView descriptionTextView = convertView.findViewById(R.id.event_description);
        ImageView imageImageView = convertView.findViewById(R.id.event_image);

        titleTextView.setText(event.getName());
        descriptionTextView.setText(event.getDescription());
        imageImageView.setImageResource(event.getImage());

        return convertView;
    }
}