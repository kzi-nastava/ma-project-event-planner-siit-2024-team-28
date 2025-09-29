package com.eventplanner.adapters.solutions;

import static android.provider.Settings.System.getString;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eventplanner.adapters.requiredSolutions.RequiredSolutionRecyclerViewAdapter;
import com.eventplanner.model.responses.solutions.GetSolutionResponse;
import com.eventplanner.R;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

public class SolutionRecyclerViewAdapter extends RecyclerView.Adapter<SolutionRecyclerViewAdapter.SolutionViewHolder>{

    private List<GetSolutionResponse> solutions;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(GetSolutionResponse solution);
    }

    public SolutionRecyclerViewAdapter(List<GetSolutionResponse> solutions, OnItemClickListener listener) {
        this.solutions = solutions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SolutionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.budget_planning_item_solution_card, parent, false);
        return new SolutionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SolutionViewHolder holder, int position) {
        GetSolutionResponse solution = solutions.get(position);
        holder.solutionName.setText(solution.getName());
        DecimalFormat df = new DecimalFormat("#.00");
        String priceLabel = holder.itemView.getContext().getString(R.string.price_input);
        holder.price.setText(priceLabel + " " + String.valueOf(df.format(solution.getPrice())) + "$");
        String priceWithDiscountLabel = holder.itemView.getContext().getString(R.string.total_price);
        Double priceWithDiscount = solution.getPrice() * (1 - solution.getDiscount() / 100);
        holder.priceWithDiscount.setText(priceWithDiscountLabel + " " + String.valueOf(df.format(priceWithDiscount)) + "$");
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
        TextView solutionName, price, priceWithDiscount;
        ImageView imageView;

        public SolutionViewHolder(@NonNull View itemView) {
            super(itemView);
            solutionName = itemView.findViewById(R.id.text_solution_name);
            price = itemView.findViewById(R.id.text_price);
            priceWithDiscount = itemView.findViewById(R.id.text_price_with_discount);
            imageView = itemView.findViewById(R.id.solution_image);
        }

    }
}
