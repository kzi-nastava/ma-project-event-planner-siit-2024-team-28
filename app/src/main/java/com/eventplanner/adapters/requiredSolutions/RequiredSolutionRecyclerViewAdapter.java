package com.eventplanner.adapters.requiredSolutions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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

        if (requiredSolution.getSolutionId() != null) {
            holder.rootLayout.setBackgroundResource(R.drawable.rounded_green_card);
        } else {
            holder.rootLayout.setBackgroundResource(R.drawable.rounded_purple_card);
        }

        holder.categoryName.setText(requiredSolution.getCategoryName());
        holder.editTextAmount.setText(String.valueOf(requiredSolution.getBudget()));

        // load solutions/solution for an item
        listener.onSolutionsRequested(requiredSolution, holder.solutionsRecycler);

        // listener for deleting an item
        holder.deleteButton.setOnClickListener(v -> {
            Long requiredSolutionId = requiredSolution.getId();
            listener.onDeleteClick(position, requiredSolutionId);
        });

        // listener for editing amount for an item
        holder.editButton.setOnClickListener(v -> {
            String newBudget = holder.editTextAmount.getText().toString().trim();
            listener.onEditClick(requiredSolution.getId(), newBudget);
        });
    }

    @Override
    public int getItemCount() {
        return requiredSolutions.size();
    }

    public GetRequiredSolutionItemResponse getItemById(Long id) {
        for (int i = 0; i < requiredSolutions.size(); i++) {
            if (requiredSolutions.get(i).getId().equals(id)) {
                return requiredSolutions.get(i);
            }
        }
        return null;
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

    public int getPositionById(Long id) {
        for (int i = 0; i < requiredSolutions.size(); i++) {
            if (requiredSolutions.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    // Interface for implementing methods that handle interactions with an item (item -> requiredSolution+)
    public interface OnItemInteractionListener {
        void onSolutionsRequested(GetRequiredSolutionItemResponse item, RecyclerView recyclerView); // recyclerView -> one that contains solutions for item
        void onDeleteClick(int position, Long requiredSolutionId);
        void onEditClick(Long requiredSolutionId, String newBudget); // newBudget is string since we want to do validation in fragment/activity
    }

    static class RequiredSolutionViewHolder extends RecyclerView.ViewHolder {
        LinearLayout rootLayout;
        TextView categoryName;
        RecyclerView solutionsRecycler;
        EditText editTextAmount;
        Button deleteButton;
        Button editButton;

        public RequiredSolutionViewHolder(@NonNull View itemView) {
            super(itemView);
            rootLayout = itemView.findViewById(R.id.root_layout);
            categoryName = itemView.findViewById(R.id.category_name);
            solutionsRecycler = itemView.findViewById(R.id.recycler_solutions);
            editTextAmount = itemView.findViewById(R.id.editText_amount);
            deleteButton = itemView.findViewById(R.id.button_delete_item);
            editButton = itemView.findViewById(R.id.button_edit_amount);
        }
    }
}
