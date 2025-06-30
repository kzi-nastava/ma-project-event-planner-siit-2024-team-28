package com.eventplanner.adapters.solutions;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.eventplanner.R;
import com.eventplanner.model.responses.solutions.GetPriceListSolutionResponse;

import java.util.List;

public class PriceListAdapter extends ArrayAdapter<GetPriceListSolutionResponse> {
    public PriceListAdapter(Context context, List<GetPriceListSolutionResponse> priceList) {
        super(context, 0, priceList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.price_list_card, parent, false);
        }

        GetPriceListSolutionResponse priceListItem = getItem(position);

        // Setting up card elements
        TextView solutionNumber = convertView.findViewById(R.id.solutionNumber);
        solutionNumber.setText("#" + String.valueOf(position + 1));
        TextView solutionName = convertView.findViewById(R.id.solutionName);
        solutionName.setText(priceListItem.getName());
        TextView finalPrice = convertView.findViewById(R.id.totalPrice);
        finalPrice.setText(finalPrice.getText() + " " + priceListItem.getFinalPrice() + "$");
        EditText priceET = convertView.findViewById(R.id.editText_price);
        priceET.setText(String.valueOf(priceListItem.getPrice()));
        EditText discountET = convertView.findViewById(R.id.editText_discount);
        discountET.setText(String.valueOf(priceListItem.getDiscount()));

        return convertView;
    }
}
