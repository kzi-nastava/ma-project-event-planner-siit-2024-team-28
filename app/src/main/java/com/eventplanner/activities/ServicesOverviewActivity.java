package com.eventplanner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.eventplanner.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class ServicesOverviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_services_overview);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        /*
         * routing to add service activity
         * */

        Button addServicesButton = findViewById(R.id.button_add_service);
        addServicesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("ServicesOverviewActivity", "Add services button clicked");
                Intent intent = new Intent(ServicesOverviewActivity.this, ServiceCreationActivity.class);
                startActivity(intent);
            }
        });

        /*
         *  clicking filter button inflates BottomSheetDialog which needs to be field with elements
         * */

        Button filterButton = findViewById(R.id.button_filter_services);
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("ServicesOverviewActivity", "Filter button clicked");
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(ServicesOverviewActivity.this);
                View dialogView = getLayoutInflater().inflate(R.layout.bottom_sheet_filter_services, null);

                /*
                 *  filling filter dialog with radiobuttons
                 * */
                String[] eventTypes = getResources().getStringArray(R.array.event_types);
                RadioGroup radioGroupCategories = dialogView.findViewById(R.id.radio_group_categories);
                for (String eventType : eventTypes) {
                    RadioButton radioButton = new RadioButton(ServicesOverviewActivity.this);
                    radioButton.setText(eventType);
                    radioButton.setButtonTintList(getResources().getColorStateList(R.color.cool_purple));

                    radioGroupCategories.addView(radioButton);
                }

                /*
                 *  filling filter dialog with checkboxes
                 * */
                LinearLayout eventTypesCheckboxes = dialogView.findViewById(R.id.event_types_checkboxes);
                for(String eventType: eventTypes){
                    CheckBox checkBox = new CheckBox(ServicesOverviewActivity.this);
                    checkBox.setText(eventType);
                    checkBox.setButtonTintList(getResources().getColorStateList(R.color.cool_purple));

                    eventTypesCheckboxes.addView(checkBox);
                }

                bottomSheetDialog.setContentView(dialogView);
                bottomSheetDialog.show();
            }
        });

        /*
         *  filling in some cards
         * */

        LinearLayout linearLayoutContainer = findViewById(R.id.cards_container);
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < 5; i++) {
            View itemView = inflater.inflate(R.layout.service_card, linearLayoutContainer, false);
            ImageButton imageButton = itemView.findViewById(R.id.edit_button);
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ServicesOverviewActivity.this, ServiceCreationActivity.class);
                    startActivity(intent);
                }
            });

            linearLayoutContainer.addView(itemView);
        }
    }
}