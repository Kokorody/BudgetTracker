package com.example.budgetest;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SettingsActivity extends AppCompatActivity {

    private TextView tvAppCreditDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        tvAppCreditDetails = findViewById(R.id.tvAppCreditDetails);

        // Set the click listener on the app credit details TextView
        tvAppCreditDetails.setOnClickListener(v -> showAppCreditDetails());

        // Bottom Navigation Setup
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Set selected item to 'Settings' programmatically
        bottomNav.setSelectedItemId(R.id.nav_settings);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // Handle Home navigation
                startActivity(new Intent(this, MainActivity.class));
                finish();  // Finish SettingsActivity to avoid back navigation to it
                return true;
            } else if (itemId == R.id.nav_Summary) {
                // Navigate to Profile
                startActivity(new Intent(this, SummaryActivity.class));
                finish();  // Finish SettingsActivity to avoid back navigation to it
                return true;
            } else if (itemId == R.id.nav_settings) {
                // Stay in Settings
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

    // This method shows the maker details when "Tap for more details" is clicked
    private void showAppCreditDetails() {
        // Create an AlertDialog to show the maker details
        new AlertDialog.Builder(this)
                .setTitle("Credits")
                .setMessage("App created by KokoRody.\n\n" +
                        "Version 1.0\n\n" +
                        "For inquiries, contact: https://github.com/Kokorody")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())  // Close the dialog
                .show();
    }
}
