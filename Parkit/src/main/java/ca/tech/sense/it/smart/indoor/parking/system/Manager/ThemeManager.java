package ca.tech.sense.it.smart.indoor.parking.system.Manager;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public class ThemeManager {
    private final SharedPreferences sharedPreferences;

    public ThemeManager(Context context) {
        sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
    }

    public boolean isDarkTheme() {
        return sharedPreferences.getBoolean("dark_theme", false);
    }

    public void applyTheme() {
        boolean isDarkTheme = isDarkTheme();
        AppCompatDelegate.setDefaultNightMode(isDarkTheme ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    public void saveThemePreference(boolean isDarkMode) {
        sharedPreferences.edit().putBoolean("dark_theme", isDarkMode).apply();
    }
}
