package ca.tech.sense.it.smart.indoor.parking.system.manager.preferenceManager;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    private final SharedPreferences sharedPreferences;

    public PreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
    }

    public boolean isPortraitLocked() {
        return sharedPreferences.getBoolean("lock_portrait", false);
    }

    public boolean isDarkTheme() {
        return sharedPreferences.getBoolean("dark_theme", false);
    }

    public boolean areNotificationsEnabled() {
        return sharedPreferences.getBoolean("notification_enabled", true);
    }

    public void setPortraitLocked(boolean isLocked) {
        sharedPreferences.edit().putBoolean("lock_portrait", isLocked).apply();
    }

    public void setDarkTheme(boolean isDark) {
        sharedPreferences.edit().putBoolean("dark_theme", isDark).apply();
    }

    public void setNotificationsEnabled(boolean isEnabled) {
        sharedPreferences.edit().putBoolean("notification_enabled", isEnabled).apply();
    }
}
