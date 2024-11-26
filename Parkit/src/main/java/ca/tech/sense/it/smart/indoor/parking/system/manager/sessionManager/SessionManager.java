package ca.tech.sense.it.smart.indoor.parking.system.manager.sessionManager;

import static ca.tech.sense.it.smart.indoor.parking.system.utility.Constants.USER_TYPE_OWNER;
import static ca.tech.sense.it.smart.indoor.parking.system.utility.Constants.USER_TYPE_USER;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import ca.tech.sense.it.smart.indoor.parking.system.firebase.FirebaseAuthSingleton;
import ca.tech.sense.it.smart.indoor.parking.system.firebase.FirestoreSingleton;
import ca.tech.sense.it.smart.indoor.parking.system.model.owner.Owner;
import ca.tech.sense.it.smart.indoor.parking.system.model.user.User;

public class SessionManager {
    private Context context;
    private static SessionManager instance; // Singleton instance
    private static final String TAG = "SessionManager";
    private static final String PREF_NAME = "user_preferences";
    private static final String KEY_USER_TOKEN = "user_authToken";
    private static final String KEY_OWNER_TOKEN = "owner_authToken";
    private static final String KEY_USER_TYPE = "user_type"; // store user type
    private static final String KEY_REMEMBER_ME = "remember_me"; // store remember me status

    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;



    private User currentUser;
    private Owner currentOwner;

    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        mAuth = FirebaseAuthSingleton.getInstance();
        db = FirestoreSingleton.getInstance();
        this.context = context.getApplicationContext();
    }

    // Singleton instance
    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }

    // Save authentication token and user type
    public void saveAuthToken(String authToken, String userType, boolean rememberMe) {
        if (authToken == null || userType == null) {
            // Handle error: one or both parameters are null
            Log.e("SessionManager", "Auth token or user type is null.");
            return;
        }

        if (USER_TYPE_OWNER.equals(userType)) {
            editor.putString(KEY_OWNER_TOKEN, authToken);
            editor.putString(KEY_USER_TYPE, userType);
        } else if (USER_TYPE_USER.equals(userType)) {
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
        return sharedPreferences.contains(KEY_USER_TOKEN);
    }

    public boolean isOwnerLoggedIn() {
        return sharedPreferences.contains(KEY_OWNER_TOKEN);
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

    public User getCurrentUser() {
        return currentUser;
    }

    public Owner getCurrentOwner() {
        return currentOwner;
    }

    // Get the user type (owner or user)
    public  String getUserType() {
        return sharedPreferences.getString(KEY_USER_TYPE, null);
    }
    public void saveUserType(String userType) {
        editor.putString(KEY_USER_TYPE, userType);
        editor.apply();
    }



    // Retrieve the "Remember Me" status
    public boolean isRememberMe() {
        return sharedPreferences.getBoolean(KEY_REMEMBER_ME, false);
    }


    // Clear session (logout)
    public void logout() {
        editor.clear();
        editor.apply();
    }

    // Fetch session data based on the user type
    public void fetchSessionData(OnSessionDataFetchedCallback callback) {
        String userType = getUserType();
        String authToken = getAuthToken();
        FirebaseUser user = mAuth.getCurrentUser();

        if (authToken == null || userType == null) {
            callback.onSessionDataFetched(null, null);
            return;
        }

        if (USER_TYPE_USER.equals(userType)) {
            fetchUserData(Objects.requireNonNull(user).getUid(), callback);
        } else if (USER_TYPE_OWNER.equals(userType)) {
            fetchOwnerData(Objects.requireNonNull(user).getUid(), callback);
        } else {
            callback.onSessionDataFetched(null, null);
        }
    }

    private void fetchUserData(String userID, OnSessionDataFetchedCallback callback) {
        db.collection("users").document(userID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            currentUser = document.toObject(User.class);
                            callback.onSessionDataFetched(currentUser, null);
                        } else {
                            callback.onSessionDataFetched(null, null);
                        }
                    } else {
                        callback.onSessionDataFetched(null, null);
                    }
                });
    }

    private void fetchOwnerData(String userID, OnSessionDataFetchedCallback callback) {
        db.collection("owners").document(userID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            currentOwner = document.toObject(Owner.class);
                            callback.onSessionDataFetched(null, currentOwner);
                        } else {
                            callback.onSessionDataFetched(null, null);
                        }
                    } else {
                        callback.onSessionDataFetched(null, null);
                    }
                });
    }

    // Interface for callback
    public interface OnSessionDataFetchedCallback {
        void onSessionDataFetched(User user, Owner owner);
    }


    public void saveUserDetails(String name, String email, Uri photoUrl) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_name", name);
        editor.putString("user_email", email);
        editor.putString("user_photo_url", photoUrl != null ? photoUrl.toString() : null);
        editor.apply();
    }

    public void printSharedPreferences(Context context) {
        // Get SharedPreferences object
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Iterate over all the keys and print their values
        for (String key : sharedPreferences.getAll().keySet()) {
            // Get the value associated with the key
            Object value = sharedPreferences.getAll().get(key);
            Log.d("SharedPreferences", "Key: " + key + ", Value: " + value);
        }
    }



}
