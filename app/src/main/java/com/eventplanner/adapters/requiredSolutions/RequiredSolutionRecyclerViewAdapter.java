package com.eventplanner.adapters.requiredSolutions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eventplanner.R;
import com.eventplanner.model.responses.requiredSolutions.GetRequiredSolutionItemResponse;

import java.util.List;

public class RequiredSolutionRecyclerViewAdapter extends RecyclerView.Adapter<RequiredSolutionRecyclerViewAdapter.RequiredSolutionViewHolder>{
    private final List<GetRequiredSolutionItemResponse> requiredSolutions;
    private final OnItemInteractionListener listener;

    public RequiredSolutionRecyclerViewAdapter(List<GetRequiredSolutionItemResponse> requiredSolutions,
                                               OnItemInteractionListener listener) {
        this.requiredSolutions = requiredSolutions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RequiredSolutionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.budget_planning_item_card, parent, false);
        return new RequiredSolutionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequiredSolutionViewHolder holder, int position) {
        GetRequiredSolutionItemResponse requiredSolution = requiredSolutions.get(position);
        holder.categoryName.setText(requiredSolution.getCategoryName());
        holder.editTextAmount.setText(String.valueOf(requiredSolution.getBudget()));

        Double budget = requiredSolutions.get(position).getBudget();
        listener.onSolutionsRequested(requiredSolution.getCategoryId(), budget, holder.solutionsRecycler);

        holder.deleteButton.setOnClickListener(v -> {
            Long requiredSolutionId = requiredSolution.getId();
            listener.onDeleteClick(position, requiredSolutionId);
        });
    }

    @Override
    public int getItemCount() {
        return requiredSolutions.size();
    }

    public void removeItemById(Long id) {
        for (int i = 0; i < requiredSolutions.size(); i++) {
            if (requiredSolutions.get(i).getId().equals(id)) {
                requiredSolutions.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    // Interface for implementing methods that handle interactions with an item (item -> requiredSolution+)
    public interface OnItemInteractionListener {
        void onSolutionsRequested(Long categoryId, Double budget, RecyclerView recyclerView); // recyclerView -> one that contains solutions for item
        void onDeleteClick(int position, Long requiredSolutionId);
    }

    static class RequiredSolutionViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName;
        RecyclerView solutionsRecycler;
        EditText editTextAmount;
        Button deleteButton;

        public RequiredSolutionViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.category_name);
            solutionsRecycler = itemView.findViewById(R.id.recycler_solutions);
            editTextAmount = itemView.findViewById(R.id.editText_amount);
            deleteButton = itemView.findViewById(R.id.button_delete_item);
        }
    }
}
