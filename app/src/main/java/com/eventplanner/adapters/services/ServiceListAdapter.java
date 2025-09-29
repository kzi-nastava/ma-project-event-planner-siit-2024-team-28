package com.eventplanner.adapters.services;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.eventplanner.R;
import com.eventplanner.model.responses.services.GetServiceResponse;
import com.eventplanner.utils.AuthUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ServiceListAdapter extends RecyclerView.Adapter<ServiceListAdapter.ServiceViewHolder> {
    private List<GetServiceResponse> services;
    private OnServiceActionListener listener;

    public interface OnServiceActionListener {
        void onEditService(GetServiceResponse service);
        void onDeleteService(GetServiceResponse service);
    }

    public ServiceListAdapter(List<GetServiceResponse> services, OnServiceActionListener listener) {
        this.services = services;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_service_list, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        GetServiceResponse service = services.get(position);
        
        holder.nameTextView.setText(service.getName());
        holder.descriptionTextView.setText(service.getDescription());
        
        DecimalFormat df = new DecimalFormat("#.00");
        holder.priceTextView.setText("$" + df.format(service.getPrice()));
        
        if (service.getDiscount() != null && service.getDiscount() > 0) {
            double discountedPrice = service.getPrice() * (1 - service.getDiscount() / 100);
            if (holder.discountedPriceTextView != null) {
                holder.discountedPriceTextView.setText("$" + df.format(discountedPrice));
                holder.discountedPriceTextView.setVisibility(View.VISIBLE);
            }
            holder.discountTextView.setText(df.format(service.getDiscount()) + "% OFF");
            holder.discountTextView.setVisibility(View.VISIBLE);
        } else {
            if (holder.discountedPriceTextView != null) {
                holder.discountedPriceTextView.setVisibility(View.GONE);
            }
            holder.discountTextView.setVisibility(View.GONE);
        }

        // Set availability and visibility status with null checks
        if (holder.availabilityTextView != null) {
            holder.availabilityTextView.setText(service.getIsAvailable() ? "Available" : "Unavailable");
            holder.availabilityTextView.setTextColor(
                    service.getIsAvailable() ?
                    holder.itemView.getContext().getColor(R.color.green) :
                    holder.itemView.getContext().getColor(R.color.red)
            );
        }

        if (holder.visibilityTextView != null) {
            holder.visibilityTextView.setText(service.getIsVisibleForEventOrganizers() ? "Visible" : "Hidden");
            holder.visibilityTextView.setTextColor(
                    service.getIsVisibleForEventOrganizers() ?
                    holder.itemView.getContext().getColor(R.color.green) :
                    holder.itemView.getContext().getColor(R.color.orange)
            );
        }

        // Set up image carousel
        if (holder.imageViewPager != null) {
            if (service.getImagesBase64() != null && !service.getImagesBase64().isEmpty()) {
                ServiceImageAdapter imageAdapter = new ServiceImageAdapter(holder.itemView.getContext(), service.getImagesBase64());
                holder.imageViewPager.setAdapter(imageAdapter);
                holder.imageViewPager.setVisibility(View.VISIBLE);
            } else {
                // Show placeholder when no images
                holder.imageViewPager.setVisibility(View.VISIBLE);
                // Create a single placeholder image list
                List<String> placeholderList = new ArrayList<>();
                placeholderList.add(""); // Empty string will show placeholder
                ServiceImageAdapter imageAdapter = new ServiceImageAdapter(holder.itemView.getContext(), placeholderList);
                holder.imageViewPager.setAdapter(imageAdapter);
            }
        }

        // Show/hide action buttons based on ownership
        Long currentUserId = AuthUtils.getUserId(holder.itemView.getContext());
        boolean isOwner = currentUserId != null && currentUserId.equals(service.getBusinessOwnerId());
        
        if (isOwner) {
            holder.editButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setVisibility(View.VISIBLE);
            
            holder.editButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditService(service);
                }
            });
            
            holder.deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteService(service);
                }
            });
        } else {
            holder.editButton.setVisibility(View.GONE);
            holder.deleteButton.setVisibility(View.GONE);
        }

        // Show category name if available
        if (service.getCategoryName() != null && !service.getCategoryName().isEmpty()) {
            holder.categoryTextView.setText(service.getCategoryName());
            holder.categoryTextView.setVisibility(View.VISIBLE);
        } else {
            holder.categoryTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return services.size();
    }



    static class ServiceViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, descriptionTextView, priceTextView, discountedPriceTextView;
        TextView discountTextView, availabilityTextView, visibilityTextView, categoryTextView;
        ViewPager2 imageViewPager;
        Button editButton, deleteButton;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.service_name);
            descriptionTextView = itemView.findViewById(R.id.service_description);
            priceTextView = itemView.findViewById(R.id.service_price);
            discountTextView = itemView.findViewById(R.id.service_discount);
            categoryTextView = itemView.findViewById(R.id.service_category);
            imageViewPager = itemView.findViewById(R.id.image_view_pager);
            editButton = itemView.findViewById(R.id.button_edit);
            deleteButton = itemView.findViewById(R.id.button_delete);

            // Note: Some views may not exist in layout, so we'll handle null checks in binding
            discountedPriceTextView = itemView.findViewById(R.id.service_discounted_price);
            availabilityTextView = itemView.findViewById(R.id.service_availability);
            visibilityTextView = itemView.findViewById(R.id.service_visibility);
        }
    }
}
