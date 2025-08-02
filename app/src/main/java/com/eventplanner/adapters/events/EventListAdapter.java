package com.eventplanner.adapters.events;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.eventplanner.R;
import com.eventplanner.model.responses.events.GetEventResponse;
import com.eventplanner.utils.Base64Util;

import java.util.List;

public class EventListAdapter extends ArrayAdapter<GetEventResponse> {
    public EventListAdapter(Context context, List<GetEventResponse> events) {
        super(context, 0, events);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.event_card, parent, false);
        }

        GetEventResponse event = getItem(position);

        TextView titleTextView = convertView.findViewById(R.id.event_name);
        TextView descriptionTextView = convertView.findViewById(R.id.event_description);
        ImageView imageImageView = convertView.findViewById(R.id.event_image);

        titleTextView.setText(event.getName());
        descriptionTextView.setText(event.getDescription());

        String profilePictureBase64 = event.getImageBase64();
        Bitmap bitmap = Base64Util.decodeBase64ToBitmap(profilePictureBase64);
        if (bitmap != null) {
            imageImageView.setImageBitmap(bitmap);
        }

        return convertView;
    }
}