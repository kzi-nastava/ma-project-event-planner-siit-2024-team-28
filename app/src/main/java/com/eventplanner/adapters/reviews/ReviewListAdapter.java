package com.eventplanner.adapters.reviews;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.eventplanner.R;
import com.eventplanner.model.responses.reviews.GetReviewPreviewResponse;

import java.util.ArrayList;
import java.util.List;

public class ReviewListAdapter extends ArrayAdapter<GetReviewPreviewResponse> {
    public ReviewListAdapter(Context context, List<GetReviewPreviewResponse> reviews) {
        super(context,0, reviews);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.review_card, parent, false);
        }

        GetReviewPreviewResponse review = getItem(position);

        TextView rating = convertView.findViewById(R.id.rating);
        TextView reviewer = convertView.findViewById(R.id.reviewer);
        TextView reviewed = convertView.findViewById(R.id.reviewed);

        rating.setText(review.getRating().toString() + "/5");
        reviewer.setText("- " + review.getReviewerName());
        reviewed.setText(review.getSolutionName());

        return convertView;
    }
}
