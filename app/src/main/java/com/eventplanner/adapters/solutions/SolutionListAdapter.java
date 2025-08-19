package com.eventplanner.adapters.solutions;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.eventplanner.R;
import com.eventplanner.model.solutions.Solution;

import java.util.List;

public class SolutionListAdapter extends ArrayAdapter<Solution> {
    public interface OnItemClickListener {
        void onItemClick(Solution solution);
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public SolutionListAdapter(Context context, List<Solution> solutions) {
        super(context, 0, solutions);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.solution_card, parent, false);
        }

        Solution solution = getItem(position);

        TextView titleTextView = convertView.findViewById(R.id.solution_name);
        TextView descriptionTextView = convertView.findViewById(R.id.solution_description);
        ImageView imageImageView = convertView.findViewById(R.id.solution_image);

        titleTextView.setText(solution.getName());
        descriptionTextView.setText(solution.getDescription());
        imageImageView.setImageResource(solution.getImage());

        // adding a onClickListener
        convertView.setOnClickListener(v -> {
            if (listener != null && solution != null) {
                listener.onItemClick(solution);
            }
        });

        return convertView;
    }
}