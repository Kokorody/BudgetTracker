package com.example.budgetest;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import android.content.SharedPreferences;
import android.content.Context;

public class SettingsActivity extends AppCompatActivity {

    private TextView tvAppCreditDetails;
    private SwitchMaterial themeSwitch;
    private SharedPreferences sharedPreferences;
    private static final String THEME_PREF = "theme_preferences";
    private static final String DARK_MODE = "dark_mode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(THEME_PREF, Context.MODE_PRIVATE);

        setContentView(R.layout.activity_settings);

        // Initialize views
        tvAppCreditDetails = findViewById(R.id.tvAppCreditDetails);
        themeSwitch = findViewById(R.id.switchTheme);

        // Set up theme switch
        setupThemeSwitch();

        // Set up app credits
        setupAppCredits();

        // Set up bottom navigation
        setupBottomNavigation();
    }

    private void setupThemeSwitch() {
        // Set the switch state based on saved preference
        boolean isDarkMode = sharedPreferences.getBoolean(DARK_MODE, false);
        themeSwitch.setChecked(isDarkMode);

        // Set up the switch listener
        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Save preference
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(DARK_MODE, isChecked);
            editor.apply();

            // Apply theme
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });
    }

    private void setupAppCredits() {
        tvAppCreditDetails.setOnClickListener(v -> showAppCreditDetails());
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Set selected item to 'Settings'
        bottomNav.setSelectedItemId(R.id.nav_settings);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_Summary) {
                startActivity(new Intent(this, SummaryActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_settings) {
                return true;
            }
            return false;
        });
    }

    private void showAppCreditDetails() {
        new AlertDialog.Builder(this)
                .setTitle("Credits")
                .setMessage("App created by KokoRody.\n\n" +
                        "Version 1.0\n\n" +
                        "For inquiries, contact: https://github.com/Kokorody")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        overridePendingTransition(0, 0);
    }
}