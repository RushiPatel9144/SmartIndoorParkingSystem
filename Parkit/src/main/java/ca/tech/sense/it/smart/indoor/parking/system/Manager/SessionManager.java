package ca.tech.sense.it.smart.indoor.parking.system.Manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SessionManager {
    private Context context;

    private static final String PREF_NAME = "user_preferences";
    private static final String KEY_USER_TOKEN = "user_authToken";
    private static final String KEY_OWNER_TOKEN = "owner_authToken";
    private static final String KEY_USER_TYPE = "user_type"; // store user type

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        this.context = context.getApplicationContext();
    }

    // Save authentication token and user type
    public void saveAuthToken(String authToken, String userType, boolean rememberMe) {
        if (authToken == null || userType == null) {
            // Handle error: one or both parameters are null
            Log.e("SessionManager", "Auth token or user type is null.");
            return;
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if ("owner".equals(userType)) {
            editor.putString(KEY_OWNER_TOKEN, authToken);
            editor.putString(KEY_USER_TYPE, userType);
        } else if ("user".equals(userType)) {
            editor.putString(KEY_USER_TOKEN, authToken);
            editor.putString(KEY_USER_TYPE, userType);
        } else {
            Log.e("SessionManager", "Invalid user type.");
            return;
        }

        editor.putBoolean("remember_me", rememberMe);
        editor.apply();
    }

    // Check if the user is logged in by checking if token exists
    public boolean isUserLoggedIn() {
        return sharedPreferences.contains(KEY_USER_TOKEN) || sharedPreferences.contains(KEY_OWNER_TOKEN);
    }

    // Retrieve the token based on the user type
    public String getAuthToken() {
        String userType = sharedPreferences.getString(KEY_USER_TYPE, null);
        if ("owner".equals(userType)) {
            return sharedPreferences.getString(KEY_OWNER_TOKEN, null);
        } else {
            return sharedPreferences.getString(KEY_USER_TOKEN, null);
        }
    }

    // Get the user type (owner or user)
    public String getUserType() {
        return sharedPreferences.getString(KEY_USER_TYPE, null);
    }
    public void saveUserType(String userType) {
        sharedPreferences.edit().putString("userType", userType).apply();
    }



    // Clear session (logout)
    public void logout() {
        editor.clear();
        editor.apply();
    }

    public boolean isOwnerLoggedIn() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
        return sharedPreferences.contains("owner_authToken");
    }

}
