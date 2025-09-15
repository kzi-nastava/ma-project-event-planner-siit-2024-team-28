package com.eventplanner.adapters.services;

import android.content.Context;
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

public class AllServicesAdapter extends RecyclerView.Adapter<AllServicesAdapter.ServiceViewHolder> {
    
    public interface OnServiceClickListener {
        void onServiceClick(GetServiceResponse service);
        void onEditService(GetServiceResponse service);
        void onDeleteService(GetServiceResponse service);
    }

    private Context context;
    private List<GetServiceResponse> services;
    private OnServiceClickListener listener;

    public AllServicesAdapter(Context context, List<GetServiceResponse> services) {
        this.context = context;
        this.services = services;
    }

    public void setOnServiceClickListener(OnServiceClickListener listener) {
        this.listener = listener;
    }

    public void updateServices(List<GetServiceResponse> newServices) {
        this.services = newServices;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        GetServiceResponse service = services.get(position);
        
        holder.nameTextView.setText(service.getName());
        holder.descriptionTextView.setText(service.getDescription());
        
        DecimalFormat df = new DecimalFormat("#.00");
        holder.priceTextView.setText("$" + df.format(service.getPrice()));
        
        // Show category if available
        if (service.getCategoryName() != null && !service.getCategoryName().isEmpty()) {
            holder.categoryTextView.setText(service.getCategoryName());
            holder.categoryTextView.setVisibility(View.VISIBLE);
        } else {
            holder.categoryTextView.setVisibility(View.GONE);
        }
        
        // Show discount if available
        if (service.getDiscount() != null && service.getDiscount() > 0) {
            double discountedPrice = service.getPrice() * (1 - service.getDiscount() / 100);
            holder.discountTextView.setText(df.format(service.getDiscount()) + "% OFF");
            holder.discountTextView.setVisibility(View.VISIBLE);
        } else {
            holder.discountTextView.setVisibility(View.GONE);
        }
        
        // Show availability status
        if (service.getIsAvailable() != null) {
            if (service.getIsAvailable()) {
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
        if (service.getIsVisibleForEventOrganizers() != null && service.getIsVisibleForEventOrganizers()) {
            holder.badgeVisible.setVisibility(View.VISIBLE);
        } else {
            holder.badgeVisible.setVisibility(View.GONE);
        }

        // Set up image carousel
        if (service.getImagesBase64() != null && !service.getImagesBase64().isEmpty()) {
            ServiceImageAdapter imageAdapter = new ServiceImageAdapter(context, service.getImagesBase64());
            holder.imageViewPager.setAdapter(imageAdapter);
        } else {
            // Show placeholder when no images
            List<String> placeholderList = new ArrayList<>();
            placeholderList.add(""); // Empty string will show placeholder
            ServiceImageAdapter imageAdapter = new ServiceImageAdapter(context, placeholderList);
            holder.imageViewPager.setAdapter(imageAdapter);
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

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onServiceClick(service);
            }
        });
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    static class ServiceViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, descriptionTextView, priceTextView, categoryTextView, discountTextView;
        TextView badgeVisible, badgeAvailable, badgeUnavailable;
        ViewPager2 imageViewPager;
        Button editButton, deleteButton;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.service_name);
            descriptionTextView = itemView.findViewById(R.id.service_description);
            priceTextView = itemView.findViewById(R.id.service_price);
            categoryTextView = itemView.findViewById(R.id.service_category);
            discountTextView = itemView.findViewById(R.id.service_discount);
            badgeVisible = itemView.findViewById(R.id.badge_visible);
            badgeAvailable = itemView.findViewById(R.id.badge_available);
            badgeUnavailable = itemView.findViewById(R.id.badge_unavailable);
            imageViewPager = itemView.findViewById(R.id.image_view_pager);
            editButton = itemView.findViewById(R.id.button_edit);
            deleteButton = itemView.findViewById(R.id.button_delete);
        }
    }
}
