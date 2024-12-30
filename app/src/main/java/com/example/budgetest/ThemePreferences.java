package com.example.budgetest;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class ThemePreferences {
    private static final String PREF_NAME = "ThemePreferences";
    private static final String KEY_THEME_MODE = "theme_mode";

    private final SharedPreferences preferences;

    public ThemePreferences(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    public void setDarkMode(boolean isDarkMode) {
        Log.d("ThemePreferences", "Setting dark mode to: " + isDarkMode);
        preferences.edit().putBoolean(KEY_THEME_MODE, isDarkMode).apply();
    }

    public boolean isDarkMode() {
        boolean isDark = preferences.getBoolean(KEY_THEME_MODE, false);
        Log.d("ThemePreferences", "Current dark mode value: " + isDark);
        return isDark;
    }
}
