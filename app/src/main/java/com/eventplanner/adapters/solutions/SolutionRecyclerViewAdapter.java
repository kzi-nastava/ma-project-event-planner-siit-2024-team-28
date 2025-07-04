package com.eventplanner.adapters.solutions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eventplanner.model.responses.solutions.GetSolutionResponse;
import com.eventplanner.R;

import java.util.List;

public class SolutionRecyclerViewAdapter extends RecyclerView.Adapter<SolutionRecyclerViewAdapter.SolutionViewHolder>{

    private List<GetSolutionResponse> solutions;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(GetSolutionResponse solution);
    }

    public SolutionRecyclerViewAdapter(List<GetSolutionResponse> solutions) {
        this.solutions = solutions;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public SolutionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.solution_card, parent, false);
        return new SolutionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SolutionViewHolder holder, int position) {
        GetSolutionResponse solution = solutions.get(position);
        holder.titleTextView.setText(solution.getName());
        holder.descriptionTextView.setText(solution.getDescription());
        // TODO: srediti slike
        // holder.imageView.setImageResource(solution.getImageBase64());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(solution);
            }
        });
    }

    @Override
    public int getItemCount() {
        return solutions.size();
    }

    static class SolutionViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, descriptionTextView;
        ImageView imageView;

        public SolutionViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.solution_name);
            descriptionTextView = itemView.findViewById(R.id.solution_description);
            imageView = itemView.findViewById(R.id.solution_image);
        }
    }
}
