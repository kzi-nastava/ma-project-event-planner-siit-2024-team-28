package com.eventplanner.adapters.solutions;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.eventplanner.R;
import com.eventplanner.model.responses.services.GetServiceResponse;

import java.text.DecimalFormat;
import java.util.List;

public class ServiceListAdapter extends ArrayAdapter<GetServiceResponse> {

    public ServiceListAdapter(Context context, List<GetServiceResponse> services) {
        super(context, 0, services);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.service_card, parent, false);
        }

        GetServiceResponse service = getItem(position);

        TextView serviceName = convertView.findViewById(R.id.serviceName);
        TextView priceText = convertView.findViewById(R.id.textPrice);
        ImageView image = convertView.findViewById(R.id.serviceImage);

        serviceName.setText(service.getName());
        DecimalFormat df = new DecimalFormat("0.##");
        priceText.setText("Price: " + df.format(service.getPrice()) + "$");
        // TODO: srediti za sliku

        Button editButton = convertView.findViewById(R.id.editButton);
        editButton.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("serviceId", String.valueOf(service.getId()));

            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_services_overview_to_service_edit, args);
        });
        return convertView;
    }
}
