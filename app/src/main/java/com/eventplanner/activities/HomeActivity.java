package com.eventplanner.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.eventplanner.R;
import com.eventplanner.fragments.FragmentTransition;
import com.eventplanner.fragments.events.TopEventsFragment;
import com.eventplanner.fragments.solutions.TopSolutionsFragment;

public class HomeActivity extends AppCompatActivity {
@Override
   public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home_base);

    Button eventsButton = findViewById(R.id.top_events_button);
    eventsButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FragmentTransition.to(TopEventsFragment.newInstance(), HomeActivity.this, false, R.id.fragment_top);
        }
    });
    Button solutionsButton = findViewById(R.id.top_solutions_button);
    solutionsButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FragmentTransition.to(TopSolutionsFragment.newInstance(), HomeActivity.this, false, R.id.fragment_top);
        }
    });





}

}
