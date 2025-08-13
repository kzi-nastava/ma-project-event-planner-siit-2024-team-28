package com.eventplanner.adapters.products;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eventplanner.R;
import com.eventplanner.adapters.products.ProductImageAdapter;
import com.eventplanner.model.responses.products.GetProductResponse;
import com.eventplanner.utils.AuthUtils;
import androidx.viewpager2.widget.ViewPager2;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class AllProductsAdapter extends RecyclerView.Adapter<AllProductsAdapter.ProductViewHolder> {
    
    public interface OnProductClickListener {
        void onProductClick(GetProductResponse product);
        void onEditProduct(GetProductResponse product);
        void onDeleteProduct(GetProductResponse product);
    }

    private Context context;
    private List<GetProductResponse> products;
    private OnProductClickListener listener;

    public AllProductsAdapter(Context context, List<GetProductResponse> products) {
        this.context = context;
        this.products = products;
    }

    public void setOnProductClickListener(OnProductClickListener listener) {
        this.listener = listener;
    }

    public void updateProducts(List<GetProductResponse> newProducts) {
        this.products = newProducts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        GetProductResponse product = products.get(position);
        
        holder.nameTextView.setText(product.getName());
        holder.descriptionTextView.setText(product.getDescription());
        
        DecimalFormat df = new DecimalFormat("#.00");
        holder.priceTextView.setText("$" + df.format(product.getPrice()));
        
        // Show category if available
        if (product.getCategoryName() != null && !product.getCategoryName().isEmpty()) {
            holder.categoryTextView.setText(product.getCategoryName());
            holder.categoryTextView.setVisibility(View.VISIBLE);
        } else {
            holder.categoryTextView.setVisibility(View.GONE);
        }
        
        // Show discount if available
        if (product.getDiscount() != null && product.getDiscount() > 0) {
            double discountedPrice = product.getPrice() * (1 - product.getDiscount() / 100);
            holder.discountTextView.setText(df.format(product.getDiscount()) + "% OFF");
            holder.discountTextView.setVisibility(View.VISIBLE);
        } else {
            holder.discountTextView.setVisibility(View.GONE);
        }
        
        // Show availability status
        if (product.getIsAvailable() != null) {
            if (product.getIsAvailable()) {
                holder.badgeAvailable.setVisibility(View.VISIBLE);
                holder.badgeUnavailable.setVisibility(View.GONE);
            } else {
                holder.badgeAvailable.setVisibility(View.GONE);
                holder.badgeUnavailable.setVisibility(View.VISIBLE);
            }
        } else {
            holder.badgeAvailable.setVisibility(View.GONE);
            holder.badgeUnavailable.setVisibility(View.GONE);
        }
        
        // Show visibility status
        if (product.getIsVisibleForEventOrganizers() != null && product.getIsVisibleForEventOrganizers()) {
            holder.badgeVisible.setVisibility(View.VISIBLE);
        } else {
            holder.badgeVisible.setVisibility(View.GONE);
        }

        // Set up image carousel
        if (product.getImagesBase64() != null && !product.getImagesBase64().isEmpty()) {
            ProductImageAdapter imageAdapter = new ProductImageAdapter(context, product.getImagesBase64());
            holder.imageViewPager.setAdapter(imageAdapter);
        } else {
            // Show placeholder when no images
            List<String> placeholderList = new ArrayList<>();
            placeholderList.add(""); // Empty string will show placeholder
            ProductImageAdapter imageAdapter = new ProductImageAdapter(context, placeholderList);
            holder.imageViewPager.setAdapter(imageAdapter);
        }

        // Show/hide action buttons based on ownership
        Long currentUserId = AuthUtils.getUserId(holder.itemView.getContext());
        boolean isOwner = currentUserId != null && currentUserId.equals(product.getBusinessOwnerId());

        if (isOwner) {
            holder.editButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setVisibility(View.VISIBLE);

            holder.editButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditProduct(product);
                }
            });

            holder.deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteProduct(product);
                }
            });
        } else {
            holder.editButton.setVisibility(View.GONE);
            holder.deleteButton.setVisibility(View.GONE);
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
        TextView nameTextView, descriptionTextView, priceTextView, categoryTextView, discountTextView;
        TextView badgeVisible, badgeAvailable, badgeUnavailable;
        ViewPager2 imageViewPager;
        Button editButton, deleteButton;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.product_name);
            descriptionTextView = itemView.findViewById(R.id.product_description);
            priceTextView = itemView.findViewById(R.id.product_price);
            categoryTextView = itemView.findViewById(R.id.product_category);
            discountTextView = itemView.findViewById(R.id.product_discount);
            badgeVisible = itemView.findViewById(R.id.badge_visible);
            badgeAvailable = itemView.findViewById(R.id.badge_available);
            badgeUnavailable = itemView.findViewById(R.id.badge_unavailable);
            imageViewPager = itemView.findViewById(R.id.image_view_pager);
            editButton = itemView.findViewById(R.id.button_edit);
            deleteButton = itemView.findViewById(R.id.button_delete);
        }
    }
}
