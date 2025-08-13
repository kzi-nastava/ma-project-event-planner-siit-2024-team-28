package com.eventplanner.adapters.products;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.eventplanner.R;
import com.eventplanner.model.responses.products.GetProductResponse;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<GetProductResponse> products;
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(GetProductResponse product);
    }

    public ProductAdapter(List<GetProductResponse> products, OnProductClickListener listener) {
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        GetProductResponse product = products.get(position);

        holder.nameTextView.setText(product.getName());
        holder.descriptionTextView.setText(product.getDescription());
        holder.priceTextView.setText(String.format("$%.2f", product.getPrice()));

        // For now, just show the ViewPager without adapter
        if (product.getImagesBase64() != null && !product.getImagesBase64().isEmpty()) {
            // TODO: Implement proper image display when ImageCarouselAdapter is available
            holder.imageViewPager.setVisibility(View.VISIBLE);
        } else {
            holder.imageViewPager.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, descriptionTextView, priceTextView;
        View imageViewPager;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.product_name);
            descriptionTextView = itemView.findViewById(R.id.product_description);
            priceTextView = itemView.findViewById(R.id.product_price);
            imageViewPager = itemView.findViewById(R.id.image_view_pager);
        }
    }
}