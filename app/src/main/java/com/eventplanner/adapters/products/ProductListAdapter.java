package com.eventplanner.adapters.products;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eventplanner.R;
import com.eventplanner.model.responses.products.GetProductResponse;
import com.eventplanner.utils.AuthUtils;
import androidx.viewpager2.widget.ViewPager2;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ProductListAdapter extends RecyclerView.Adapter<ProductListAdapter.ProductViewHolder> {
    private final List<GetProductResponse> products;
    private final OnProductActionListener listener;

    public interface OnProductActionListener {
        void onEditProduct(GetProductResponse product);
        void onDeleteProduct(GetProductResponse product);
    }

    public ProductListAdapter(List<GetProductResponse> products, OnProductActionListener listener) {
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_list, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        GetProductResponse product = products.get(position);
        
        holder.nameTextView.setText(product.getName());
        holder.descriptionTextView.setText(product.getDescription());
        
        DecimalFormat df = new DecimalFormat("#.00");
        holder.priceTextView.setText(String.format("$%s", df.format(product.getPrice())));
        
        if (product.getDiscount() != null && product.getDiscount() > 0) {
            double discountedPrice = product.getPrice() * (1 - product.getDiscount() / 100);
            if (holder.discountedPriceTextView != null) {
                holder.discountedPriceTextView.setText(String.format("$%s", df.format(discountedPrice)));
                holder.discountedPriceTextView.setVisibility(View.VISIBLE);
            }
            holder.discountTextView.setText(String.format("%s%% OFF", df.format(product.getDiscount())));
            holder.discountTextView.setVisibility(View.VISIBLE);
        } else {
            if (holder.discountedPriceTextView != null) {
                holder.discountedPriceTextView.setVisibility(View.GONE);
            }
            holder.discountTextView.setVisibility(View.GONE);
        }

        // Set availability and visibility status with null checks
        if (holder.availabilityTextView != null) {
            holder.availabilityTextView.setText(product.getIsAvailable() ? "Available" : "Unavailable");
            holder.availabilityTextView.setTextColor(
                    product.getIsAvailable() ?
                    holder.itemView.getContext().getColor(R.color.green) :
                    holder.itemView.getContext().getColor(R.color.red)
            );
        }

        if (holder.visibilityTextView != null) {
            holder.visibilityTextView.setText(product.getIsVisibleForEventOrganizers() ? "Visible" : "Hidden");
            holder.visibilityTextView.setTextColor(
                    product.getIsVisibleForEventOrganizers() ?
                    holder.itemView.getContext().getColor(R.color.green) :
                    holder.itemView.getContext().getColor(R.color.orange)
            );
        }

        // Set up image carousel
        if (holder.imageViewPager != null) {
            if (product.getImagesBase64() != null && !product.getImagesBase64().isEmpty()) {
                ProductImageAdapter imageAdapter = new ProductImageAdapter(holder.itemView.getContext(), product.getImagesBase64());
                holder.imageViewPager.setAdapter(imageAdapter);
                holder.imageViewPager.setVisibility(View.VISIBLE);
            } else {
                // Show placeholder when no images
                holder.imageViewPager.setVisibility(View.VISIBLE);
                // Create a single placeholder image list
                List<String> placeholderList = new ArrayList<>();
                placeholderList.add(""); // Empty string will show placeholder
                ProductImageAdapter imageAdapter = new ProductImageAdapter(holder.itemView.getContext(), placeholderList);
                holder.imageViewPager.setAdapter(imageAdapter);
            }
        }

        // Show/hide action buttons based on ownership
        Long currentUserId = AuthUtils.getUserId(holder.itemView.getContext());
        boolean isOwner = currentUserId != null && currentUserId.equals(product.getBusinessOwnerId());

        if (holder.editButton != null && holder.deleteButton != null) {
            holder.editButton.setVisibility(isOwner ? View.VISIBLE : View.GONE);
            holder.deleteButton.setVisibility(isOwner ? View.VISIBLE : View.GONE);

            // Always attach listeners
            holder.editButton.setOnClickListener(v -> {
                if (listener != null) listener.onEditProduct(product);
            });

            holder.deleteButton.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteProduct(product);
            });
        }

        // Show category name if available
        if (product.getCategoryName() != null && !product.getCategoryName().isEmpty()) {
            holder.categoryTextView.setText(product.getCategoryName());
            holder.categoryTextView.setVisibility(View.VISIBLE);
        } else {
            holder.categoryTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, descriptionTextView, priceTextView, discountedPriceTextView;
        TextView discountTextView, availabilityTextView, visibilityTextView, categoryTextView;
        ViewPager2 imageViewPager;
        Button editButton, deleteButton;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.product_name);
            descriptionTextView = itemView.findViewById(R.id.product_description);
            priceTextView = itemView.findViewById(R.id.product_price);
            discountTextView = itemView.findViewById(R.id.product_discount);
            categoryTextView = itemView.findViewById(R.id.product_category);
            imageViewPager = itemView.findViewById(R.id.image_view_pager);
            editButton = itemView.findViewById(R.id.edit_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
            discountedPriceTextView = itemView.findViewById(R.id.product_discounted_price);
            availabilityTextView = itemView.findViewById(R.id.product_availability);
            visibilityTextView = itemView.findViewById(R.id.product_visibility);
        }
    }
}
