package com.eventplanner.adapters.services;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eventplanner.R;
import com.eventplanner.utils.Base64Util;

import java.util.List;

public class ServiceImageAdapter extends RecyclerView.Adapter<ServiceImageAdapter.ImageViewHolder> {
    
    private Context context;
    private List<String> base64Images;

    public ServiceImageAdapter(Context context, List<String> base64Images) {
        this.context = context;
        this.base64Images = base64Images;
    }

    public void updateImages(List<String> newImages) {
        this.base64Images = newImages;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_service_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String base64Image = base64Images.get(position);
        
        if (base64Image != null && !base64Image.isEmpty()) {
            Bitmap bitmap = Base64Util.decodeBase64ToBitmap(base64Image);
            if (bitmap != null) {
                holder.imageView.setImageBitmap(bitmap);
            } else {
                // Show placeholder if decoding fails
                holder.imageView.setImageResource(R.drawable.ic_image_placeholder);
            }
        } else {
            // Show placeholder for empty/null images
            holder.imageView.setImageResource(R.drawable.ic_image_placeholder);
        }
    }

    @Override
    public int getItemCount() {
        return base64Images != null ? base64Images.size() : 0;
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.service_image);
        }
    }
}
