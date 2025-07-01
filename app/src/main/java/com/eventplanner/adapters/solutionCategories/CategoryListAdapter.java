package com.eventplanner.adapters.solutionCategories;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.eventplanner.R;
import com.eventplanner.model.responses.solutionCateogries.GetSolutionCategoryResponse;

import java.util.List;

public class CategoryListAdapter extends ArrayAdapter<GetSolutionCategoryResponse> {
    public CategoryListAdapter(Context context, List<GetSolutionCategoryResponse> categories) {
        super(context,0, categories);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.category_card,parent,false);
        }

        GetSolutionCategoryResponse category = getItem(position);

        TextView categoryName = convertView.findViewById(R.id.categoryName);
        TextView categoryDescription = convertView.findViewById(R.id.categoryDescription);

        categoryName.setText(category.getName());
        categoryDescription.setText("Description: " + category.getDescription());

        return convertView;
    }
}
