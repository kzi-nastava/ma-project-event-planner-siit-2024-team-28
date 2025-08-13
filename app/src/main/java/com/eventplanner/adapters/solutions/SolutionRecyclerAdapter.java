package com.eventplanner.adapters.solutions;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eventplanner.R;
import com.eventplanner.model.solutions.Solution;

import java.util.List;

public class SolutionRecyclerAdapter extends RecyclerView.Adapter<SolutionRecyclerAdapter.SolutionViewHolder> {
    
    public interface OnItemClickListener {
        void onItemClick(Solution solution);
    }

    private Context context;
    private List<Solution> solutions;
    private OnItemClickListener listener;

    public SolutionRecyclerAdapter(Context context, List<Solution> solutions) {
        this.context = context;
        this.solutions = solutions;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public SolutionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new SolutionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SolutionViewHolder holder, int position) {
        Solution solution = solutions.get(position);
        
        holder.nameTextView.setText(solution.getName());
        holder.descriptionTextView.setText(solution.getDescription());
        holder.priceTextView.setText(String.format("$%.2f", solution.getPrice()));

        // For now, we'll skip the image display since the layout uses ViewPager2
        // TODO: Implement proper image display when needed

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
        TextView nameTextView, descriptionTextView, priceTextView;

        public SolutionViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.product_name);
            descriptionTextView = itemView.findViewById(R.id.product_description);
            priceTextView = itemView.findViewById(R.id.product_price);
        }
    }
}
