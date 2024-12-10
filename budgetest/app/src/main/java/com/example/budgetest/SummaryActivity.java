package com.example.budgetest;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SummaryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        // Bottom Navigation Setup
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Set selected item to 'Settings' programmatically
        bottomNav.setSelectedItemId(R.id.nav_Summary);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // Handle Home navigation
                startActivity(new Intent(this, MainActivity.class));
                finish();  // Finish SettingsActivity to avoid back navigation to it
                return true;
            } else if (itemId == R.id.nav_Summary) {
                // STAY
                return true;
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                finish();  // Finish SettingsActivity to avoid back navigation to it
                return true;
            }

            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        overridePendingTransition(0, 0); // This will disable the transition animation
    }
}

