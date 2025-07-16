package com.eventplanner.adapters.solutions;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.eventplanner.R;
import com.eventplanner.model.responses.services.DeleteServiceResponse;
import com.eventplanner.model.responses.services.GetServiceResponse;
import com.eventplanner.services.ServiceService;
import com.eventplanner.utils.HttpUtils;
import com.google.gson.Gson;

import java.text.DecimalFormat;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServiceListAdapter extends ArrayAdapter<GetServiceResponse> {
    private ServiceService serviceService;

    public ServiceListAdapter(Context context, List<GetServiceResponse> services) {
        super(context, 0, services);
        serviceService = HttpUtils.getServiceService();
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

        Button deleteButton = convertView.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(v -> {
            GetServiceResponse serviceToDelete = getItem(position);

            // TODO: refactor so that adapter implements interfaces that handle Http Requests in fragments
            serviceService.deleteService(serviceToDelete.getId()).enqueue(new Callback<DeleteServiceResponse>() {
                @Override
                public void onResponse(Call<DeleteServiceResponse> call, Response<DeleteServiceResponse> response) {
                    DeleteServiceResponse responseBody = response.body();

                    if (responseBody != null) {
                        if (responseBody.getSuccess()) {
                            remove(serviceToDelete);
                            notifyDataSetChanged();
                        }
                        Toast.makeText(getContext(), responseBody.getMessage(), Toast.LENGTH_SHORT).show();
                    } else if (response.errorBody() != null) {
                        try {
                            Gson gson = new Gson();
                            DeleteServiceResponse errorResponse = gson.fromJson(
                                    response.errorBody().charStream(), DeleteServiceResponse.class
                            );

                            Toast.makeText(getContext(), errorResponse.getMessage(), Toast.LENGTH_SHORT).show();

                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Unexpected error while deleting service.", Toast.LENGTH_SHORT).show();
                            Log.e("ServiceListAdapter", "Error parsing errorBody: " + e.getMessage());
                        }
                    } else {
                        Toast.makeText(getContext(), "Unexpected error while deleting service.", Toast.LENGTH_SHORT).show();
                        Log.i("ServiceListAdapter", "Unexpected response code: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<DeleteServiceResponse> call, Throwable t) {
                    Log.i("ServiceListAdapter", "Network failure: " + t.getMessage());
                    Toast.makeText(getContext(), "There has been an error while deleting service.", Toast.LENGTH_SHORT).show();
                }
            });
        });

        return convertView;
    }
}
