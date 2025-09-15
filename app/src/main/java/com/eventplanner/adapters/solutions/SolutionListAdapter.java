package com.eventplanner.adapters.solutions;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.eventplanner.R;
import com.eventplanner.model.responses.solutions.GetSolutionResponse;
import com.eventplanner.model.solutions.Solution;
import com.eventplanner.utils.Base64Util;

import java.util.List;

public class SolutionListAdapter extends ArrayAdapter<GetSolutionResponse> {
    public SolutionListAdapter(Context context, List<GetSolutionResponse> solutions) {
        super(context, 0, solutions);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.solution_card, parent, false);
        }

        GetSolutionResponse solution = getItem(position);

        TextView titleTextView = convertView.findViewById(R.id.solution_name);
        TextView descriptionTextView = convertView.findViewById(R.id.solution_description);
        ImageView imageImageView = convertView.findViewById(R.id.solution_image);

        titleTextView.setText(solution.getName());
        descriptionTextView.setText(solution.getDescription());

        List<String> solutionImages = solution.getImageBase64();
        if(!solutionImages.isEmpty()) {
            Bitmap bitmap = Base64Util.decodeBase64ToBitmap(solutionImages.get(0));
            if (bitmap != null) {
                imageImageView.setImageBitmap(bitmap);
            }
        }

        return convertView;
    }
}