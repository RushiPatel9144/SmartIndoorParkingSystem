package ca.tech.sense.it.smart.indoor.parking.system.manager.sessionManager;

import static ca.tech.sense.it.smart.indoor.parking.system.utility.AppConstants.COLLECTION_OWNER;
import static ca.tech.sense.it.smart.indoor.parking.system.utility.AppConstants.COLLECTION_USER;
import static ca.tech.sense.it.smart.indoor.parking.system.utility.AppConstants.USER_TYPE_OWNER;
import static ca.tech.sense.it.smart.indoor.parking.system.utility.AppConstants.USER_TYPE_USER;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
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

    private SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        mAuth = FirebaseAuthSingleton.getInstance();
        db = FirestoreSingleton.getInstance();
        this.context = context.getApplicationContext();
    }

    // Singleton instance
    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            Log.d("SessionManager", "Creating new instance of SessionManager");
            instance = new SessionManager(context.getApplicationContext());
        } else {
            Log.d("SessionManager", "Returning existing instance of SessionManager");
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

        editor.putBoolean(KEY_REMEMBER_ME, rememberMe);
        editor.apply();

    }

    // Check if the user is logged in by checking if token exists
    public boolean isUserLoggedIn() {
        return sharedPreferences.contains(KEY_USER_TOKEN);
    }

    public boolean isOwnerLoggedIn() {
        return sharedPreferences.contains(KEY_OWNER_TOKEN);
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
        FirebaseUser user = mAuth.getCurrentUser();

        if (userType == null || user == null) {
            callback.onSessionDataFetched(null, null);
            return;
        }

        String collection = USER_TYPE_USER.equals(userType) ? COLLECTION_USER : USER_TYPE_OWNER.equals(userType) ? COLLECTION_OWNER : null;

        if (collection == null) {
            callback.onSessionDataFetched(null, null);
            return;
        }

        fetchDocumentData(collection, user.getUid(), userType, callback);
    }

    private void fetchDocumentData(String collection, String userID, String userType, OnSessionDataFetchedCallback callback) {
        db.collection(collection).document(userID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    if (USER_TYPE_USER.equals(userType)) {
                        currentUser = document.toObject(User.class);
                        callback.onSessionDataFetched(currentUser, null);
                    } else {
                        currentOwner = document.toObject(Owner.class);
                        callback.onSessionDataFetched(null, currentOwner);
                    }
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

}
