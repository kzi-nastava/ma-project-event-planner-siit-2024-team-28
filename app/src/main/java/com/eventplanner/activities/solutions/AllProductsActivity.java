package com.eventplanner.activities.solutions;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.eventplanner.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class AllProductsActivity extends AppCompatActivity {
@Override
   public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_all_products);
    Button filterButton = findViewById(R.id.button_filter_products);
    filterButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.i("ServicesOverviewActivity", "Filter button clicked");
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(AllProductsActivity.this);
            View dialogView = getLayoutInflater().inflate(R.layout.bottom_sheet_filter_products, null);

            /*
             *  filling filter dialog with radiobuttons
             * */
            String[] eventTypes = getResources().getStringArray(R.array.event_types);
            RadioGroup radioGroupCategories = dialogView.findViewById(R.id.radio_group_categories);
            for (String eventType : eventTypes) {
                RadioButton radioButton = new RadioButton(AllProductsActivity.this);
                radioButton.setText(eventType);
                radioButton.setButtonTintList(getResources().getColorStateList(R.color.cool_purple));

                radioGroupCategories.addView(radioButton);
            }

            /*
             *  filling filter dialog with checkboxes
             * */
            LinearLayout eventTypesCheckboxes = dialogView.findViewById(R.id.event_types_checkboxes);
            for(String eventType: eventTypes){
                CheckBox checkBox = new CheckBox(AllProductsActivity.this);
                checkBox.setText(eventType);
                checkBox.setButtonTintList(getResources().getColorStateList(R.color.cool_purple));

                eventTypesCheckboxes.addView(checkBox);
            }

            bottomSheetDialog.setContentView(dialogView);
            bottomSheetDialog.show();
        }
    });
    }
}
