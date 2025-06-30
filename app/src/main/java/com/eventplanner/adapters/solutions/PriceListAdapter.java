package com.eventplanner.adapters.solutions;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.eventplanner.R;
import com.eventplanner.model.responses.solutions.GetPriceListSolutionResponse;

import java.text.DecimalFormat;
import java.util.List;

public class PriceListAdapter extends ArrayAdapter<GetPriceListSolutionResponse> {
    private List<GetPriceListSolutionResponse> priceList;

    private OnPriceDiscountEditListener editListener;

    public PriceListAdapter(Context context, List<GetPriceListSolutionResponse> priceList) {
        super(context, 0, priceList);
        this.priceList = priceList;
    }


    // Interface for implementing listeners in Fragments
    public interface OnPriceDiscountEditListener {
        void onEditPriceClick(Long solutionId, Double newPrice);
        void onEditDiscountClick(Long solutionId, Double newDiscount);
    }

    public void setOnPriceDiscountEditListener(OnPriceDiscountEditListener listener) {
        this.editListener = listener;
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
        DecimalFormat df = new DecimalFormat("0.##");
        TextView finalPrice = convertView.findViewById(R.id.totalPrice);
        finalPrice.setText("Total price: " + df.format(priceListItem.getFinalPrice()) + "$");
        EditText priceET = convertView.findViewById(R.id.editText_price);
        priceET.setText(String.valueOf(priceListItem.getPrice()));
        EditText discountET = convertView.findViewById(R.id.editText_discount);
        discountET.setText(String.valueOf(priceListItem.getDiscount()));

        // Setting up listeners
        Button editPriceButton = convertView.findViewById(R.id.edit_price_button);
        Button editDiscountButton = convertView.findViewById(R.id.edit_discount_button);
        editPriceButton.setOnClickListener(v -> {
            if (editListener != null && priceListItem != null) {
                try {
                    Double newPrice = Double.parseDouble(priceET.getText().toString().trim());
                    editListener.onEditPriceClick(priceListItem.getSolutionId(), newPrice);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Invalid price input", Toast.LENGTH_SHORT).show();
                }
            }
        });
        editDiscountButton.setOnClickListener(v -> {
            if (editListener != null && priceListItem != null) {
                try {
                    Double newDiscount = Double.parseDouble(discountET.getText().toString().trim());
                    editListener.onEditDiscountClick(priceListItem.getSolutionId(), newDiscount);
                }  catch (Exception e) {
                    Toast.makeText(getContext(), "Invalid discount input", Toast.LENGTH_SHORT).show();
                }
            }
        });


        return convertView;
    }

    public void updatePrice(Long solutionId, Double newPrice) {
        for (GetPriceListSolutionResponse item : priceList) {
            if (item.getSolutionId().equals(solutionId)) {
                item.setPrice(newPrice);
                Double discount = item.getDiscount();
                Double finalPrice = newPrice * (1 - discount / 100);
                item.setFinalPrice(finalPrice);
                break;
            }
        }
        notifyDataSetChanged();
    }

    public void updateDiscount(Long solutionId, double newDiscount) {
        for (GetPriceListSolutionResponse item : priceList) {
            if (item.getSolutionId().equals(solutionId)) {
                item.setDiscount(newDiscount);
                Double price = item.getPrice();
                Double finalPrice = price * (1 - newDiscount / 100);
                item.setFinalPrice(finalPrice);
                break;
            }
        }
        notifyDataSetChanged();
    }
}
