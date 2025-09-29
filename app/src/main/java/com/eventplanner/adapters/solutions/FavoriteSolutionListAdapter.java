package com.eventplanner.adapters.solutions;

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
import com.eventplanner.model.responses.solutions.GetSolutionResponse;
import com.eventplanner.utils.Base64Util;

import java.util.List;

public class FavoriteSolutionListAdapter extends ArrayAdapter<GetSolutionResponse> {
    
    public FavoriteSolutionListAdapter(Context context, List<GetSolutionResponse> solutions) {
        super(context, 0, solutions);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.solution_card, parent, false);
        }

        GetSolutionResponse solution = getItem(position);

        TextView titleTextView = convertView.findViewById(R.id.solution_name);
        TextView descriptionTextView = convertView.findViewById(R.id.solution_description);
        ImageView imageImageView = convertView.findViewById(R.id.solution_image);

        if (solution != null) {
            titleTextView.setText(solution.getName());
            descriptionTextView.setText(solution.getDescription());

            // Handle image display
            if (solution.getImageBase64() != null && !solution.getImageBase64().isEmpty()) {
                String imageBase64 = solution.getImageBase64().get(0);
                Bitmap bitmap = Base64Util.decodeBase64ToBitmap(imageBase64);
                if (bitmap != null) {
                    imageImageView.setImageBitmap(bitmap);
                } else {
                    // Set default image based on solution type
                    setDefaultImage(imageImageView, solution.getType());
                }
            } else {
                // Set default image based on solution type
                setDefaultImage(imageImageView, solution.getType());
            }
        }

        return convertView;
    }

    private void setDefaultImage(ImageView imageView, String solutionType) {
        if ("Service".equals(solutionType)) {
            imageView.setImageResource(R.drawable.ketering); // Default service image
        } else {
            imageView.setImageResource(R.drawable.img_1); // Default product image
        }
    }
}
