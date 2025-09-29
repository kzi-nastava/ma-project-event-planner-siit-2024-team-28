package com.eventplanner.adapters.services;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eventplanner.R;
import com.eventplanner.model.responses.services.GetServiceResponse;

import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {
    private List<GetServiceResponse> services;
    private OnServiceClickListener listener;

    public interface OnServiceClickListener {
        void onServiceClick(GetServiceResponse service);
    }

    public ServiceAdapter(List<GetServiceResponse> services, OnServiceClickListener listener) {
        this.services = services;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        GetServiceResponse service = services.get(position);

        holder.nameTextView.setText(service.getName());
        holder.descriptionTextView.setText(service.getDescription());
        holder.priceTextView.setText(String.format("$%.2f", service.getPrice()));

        // For now, just show the ViewPager without adapter
        if (service.getImagesBase64() != null && !service.getImagesBase64().isEmpty()) {
            // TODO: Implement proper image display when ImageCarouselAdapter is available
            holder.imageViewPager.setVisibility(View.VISIBLE);
        } else {
            holder.imageViewPager.setVisibility(View.VISIBLE);
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
        TextView nameTextView, descriptionTextView, priceTextView;
        View imageViewPager;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.service_name);
            descriptionTextView = itemView.findViewById(R.id.service_description);
            priceTextView = itemView.findViewById(R.id.service_price);
            imageViewPager = itemView.findViewById(R.id.image_view_pager);
        }
    }
}